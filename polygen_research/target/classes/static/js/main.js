const sidebar = document.getElementById('sidebar');
const sidebarToggleBtn = document.getElementById('sidebar-toggle');
const sidebarToggleIcon = sidebarToggleBtn ? sidebarToggleBtn.querySelector('i') : null;

// Función para mostrar/ocultar
function toggleMenu() {
    if (!sidebar || !sidebarToggleIcon) {
        console.error("Elementos del sidebar (#sidebar) o botón (#sidebar-toggle > i) no encontrados.");
        return;
    }

    sidebar.classList.toggle('open');

    const isExpanded = sidebar.classList.contains('open');

    localStorage.setItem('sidebarState', isExpanded ? 'expanded' : 'collapsed');

    if (isExpanded) {
        sidebarToggleIcon.classList.remove('ph-caret-double-right');
        sidebarToggleIcon.classList.add('ph-caret-double-left');
        sidebarToggleBtn.setAttribute('title', 'Colapsar Menú');
    } else {
        sidebarToggleIcon.classList.remove('ph-caret-double-left');
        sidebarToggleIcon.classList.add('ph-caret-double-right');
        sidebarToggleBtn.setAttribute('title', 'Expandir Menú');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    if (!sidebar || !sidebarToggleBtn || !sidebarToggleIcon) return;

    document.body.classList.add('preload-transitions');

    const savedState = localStorage.getItem('sidebarState');

    if (savedState === 'expanded') {
        sidebar.classList.add('open');
        sidebarToggleIcon.classList.remove('ph-caret-double-right');
        sidebarToggleIcon.classList.add('ph-caret-double-left');
        sidebarToggleBtn.setAttribute('title', 'Colapsar Menú');
    } else {
        sidebar.classList.remove('open');
        sidebarToggleIcon.classList.remove('ph-caret-double-left');
        sidebarToggleIcon.classList.add('ph-caret-double-right');
        sidebarToggleBtn.setAttribute('title', 'Expandir Menú');
    }

    setTimeout(() => {
        document.body.classList.remove('preload-transitions');
    }, 50);

    function setupRadioDeselect() {
        const radios = document.querySelectorAll('.radio-deselect');
    
        radios.forEach(radio => {
            radio.addEventListener('mousedown', function() {
                this.dataset.wasChecked = this.checked;
            });
      
            radio.addEventListener('click', function() {
                if (this.dataset.wasChecked === 'true') {
                    this.checked = false;
                    this.dataset.wasChecked = 'false';
                }
            });
        });
    }
    setupRadioDeselect();
});

const navLinks = document.querySelectorAll('#sidebar nav a');
const currentPath = window.location.pathname;

navLinks.forEach(link => {
    const linkHref = link.getAttribute('href');
    if (linkHref && (linkHref === currentPath || (linkHref !== '/' && currentPath.startsWith(linkHref)))) {
        link.classList.add('active');
    }

    link.addEventListener('click', (e) => {
        if (window.innerWidth <= 768) {
            const sidebar = document.getElementById('sidebar');
            if (sidebar) {
                sidebar.classList.remove('open');
            }
        }
        // No se usa e.preventDefault(), se deja que el enlace (th:href) funcione.
    });
});

/*Lógica para Modales */

const modalPaciente = document.getElementById('modal-ver-paciente');
const modalCrf = document.getElementById('modal-ver-crf');

// Función asíncrona para mostrar detalles de un Paciente
async function verPaciente(pacienteId) {
    if (!modalPaciente) {
        console.error("El modal 'modal-ver-paciente' no se encuentra en esta página.");
        return;
    }

    // Referencia al nuevo contenedor de datos CRF
    const crfDataContainer = document.getElementById('modal-pac-crf-data');

    document.getElementById('modal-patient-id').innerText = 'Cargando...';
    // Limpia campos anteriores
    document.getElementById('modal-pac-codigo').innerText = '';
    document.getElementById('modal-pac-nombre').innerText = '';
    document.getElementById('modal-pac-apellido').innerText = '';
    document.getElementById('modal-pac-estado').innerText = '';
    crfDataContainer.innerHTML = '<p>Cargando datos de estudio...</p>'; // Limpia datos CRF
    modalPaciente.style.display = 'block';

    try {
        // Llama a la API
        const response = await fetch(`/pacientes/api/paciente/${pacienteId}`);
        if (!response.ok) {
            throw new Error(`Paciente no encontrado (ID: ${pacienteId}), Estado: ${response.status}`);
        }
        const paciente = await response.json();

        document.getElementById('modal-patient-id').innerText = `Detalles del Paciente: ${paciente.codigoPaciente || 'N/A'}`;
        document.getElementById('modal-pac-codigo').innerText = paciente.codigoPaciente || 'N/A';
        document.getElementById('modal-pac-nombre').innerText = paciente.nombre || 'N/A';
        document.getElementById('modal-pac-apellido').innerText = paciente.apellido || 'N/A';
        document.getElementById('modal-pac-estado').innerText = paciente.estado || 'N/A';

        // Logica CRF
        if (paciente.crfs && paciente.crfs.length > 0) {
            let html = '';

            // Itera sobre cada CRF del paciente
            for (const crf of paciente.crfs) {
                html += `<div class="card" style="margin-top: 1rem; background-color: var(--color-fondo);">
                           <div class="card-header" style="padding: 0.75rem 1rem;">
                             <strong>CRF ID: ${crf.idCrf}</strong>
                             <a href="/crf/editar/${crf.idCrf}" class="btn-secundario btn-sm">Editar CRF</a>
                           </div>
                           <div class="card-body" style="padding: 1rem;">`;

                // Revisa si este CRF tiene datos dinámicos
                if (crf.datosCrfList && crf.datosCrfList.length > 0) {

                    // tera sobre cada dato
                    for (const dato of crf.datosCrfList) {
                        html += `<div class="patient-data-grid" style="gap: 0.5rem 1rem;">
                                   <strong>${dato.campoCrf ? dato.campoCrf.nombre : 'Campo desconocido'}:</strong>
                                   <span>${dato.valor || '(Sin respuesta)'}</span>
                                 </div>`;
                    }
                } else {
                    html += '<p>Este CRF no tiene datos registrados.</p>';
                }
                html += '</div></div>';
            }
            crfDataContainer.innerHTML = html; // Inserta todo el HTML en el div

        } else {
            // Si el paciente no tiene CRFs
            crfDataContainer.innerHTML = '<p>Este paciente no tiene ningún CRF asociado.</p>';
        }

    } catch (error) {
        document.getElementById('modal-patient-id').innerText = 'Error al cargar los datos';
        crfDataContainer.innerHTML = '<p style="color: var(--color-error);">Error al cargar datos de estudio.</p>';
        console.error("Error en verPaciente:", error);
    }
}

// Función para cerrar el modal de Paciente
function cerrarModal() {
    if (modalPaciente) {
        modalPaciente.style.display = 'none';
    }
}

// Función asíncrona para mostrar detalles de un CRF
async function verCrf(crfId) {
    if (!modalCrf) {
        console.error("El modal 'modal-ver-crf' no se encuentra en esta página.");
        return;
    }
    document.getElementById('modal-crf-titulo').innerText = 'Cargando...';
    document.getElementById('modal-crf-id').innerText = '';
    document.getElementById('modal-crf-paciente').innerText = '';
    document.getElementById('modal-crf-grupo').innerText = '';
    document.getElementById('modal-crf-fecha').innerText = '';
    document.getElementById('modal-crf-estado').innerText = '';
    document.getElementById('modal-crf-obs').innerText = '';
    modalCrf.style.display = 'block';

    try {
        // Realiza petición fetch al endpoint del backend para CRF
        const response = await fetch(`/crfs/api/crf/${crfId}`);
        if (!response.ok) {
            throw new Error(`CRF no encontrado (ID: ${crfId}), Estado: ${response.status}`);
        }
        const crf = await response.json(); // Convierte la respuesta JSON

        // Rellena el modal con los datos del CRF
        document.getElementById('modal-crf-titulo').innerText = `Detalles del CRF: ${crf.idCrf || 'N/A'}`;
        document.getElementById('modal-crf-id').innerText = crf.idCrf || 'N/A';
        // Accede a los datos del paciente anidado
        document.getElementById('modal-crf-paciente').innerText = crf.datosPaciente ? (crf.datosPaciente.codigoPaciente || 'N/A') : 'N/A';
        document.getElementById('modal-crf-grupo').innerText = crf.grupo || 'N/A';
        document.getElementById('modal-crf-fecha').innerText = crf.fechaConsulta || 'N/A';
        document.getElementById('modal-crf-estado').innerText = crf.estado || 'N/A';
        document.getElementById('modal-crf-obs').innerText = crf.observacion || 'N/A';

    } catch (error) {
        document.getElementById('modal-crf-titulo').innerText = 'Error al cargar los datos';
        console.error("Error en verCrf:", error);
    }
}

// Función para cerrar el modal de CRF
function cerrarModalCrf() {
    if (modalCrf) {
        modalCrf.style.display = 'none';
    }
}

// Listener global para cerrar los modales si se hace clic fuera de su contenido.
window.onclick = function (event) {
    if (event.target == modalPaciente) {
        cerrarModal();
    }
    if (event.target == modalCrf) {
        cerrarModalCrf();
    }
}

/* Lógica de Personalización Para el Modo Oscuro */
const darkModeToggle = document.getElementById('dark-mode-toggle');

if (darkModeToggle) {
    darkModeToggle.addEventListener('change', () => {
        document.documentElement.classList.toggle('dark-mode');

        if (document.documentElement.classList.contains('dark-mode')) {
            localStorage.setItem('theme', 'dark');
        } else {
            localStorage.setItem('theme', 'light');
        }
    });

    if (localStorage.getItem('theme') === 'dark') {
        if (darkModeToggle) {
            darkModeToggle.checked = true;
        }
    }
}

function cambiarColorPrimario(color) {
    if (color) {
        document.documentElement.style.setProperty('--color-primario', color);
        localStorage.setItem('primaryColor', color);
    }
}

const savedColor = localStorage.getItem('primaryColor');
if (savedColor) {
    cambiarColorPrimario(savedColor);
}
