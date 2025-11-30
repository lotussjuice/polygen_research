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
            radio.addEventListener('mousedown', function () {
                this.dataset.wasChecked = this.checked;
            })
            radio.addEventListener('click', function () {
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
    });
});

const modalPaciente = document.getElementById('modal-ver-paciente');
const modalCrf = document.getElementById('modal-ver-crf');

async function verPaciente(pacienteId) {
    if (!modalPaciente) {
        console.error("El modal 'modal-ver-paciente' no se encuentra en esta página.");
        return;
    }

    const crfDataContainer = document.getElementById('modal-pac-crf-data');

    document.getElementById('modal-patient-id').innerText = 'Cargando...';
    document.getElementById('modal-pac-codigo').innerText = '';
    document.getElementById('modal-pac-nombre').innerText = '';
    document.getElementById('modal-pac-apellido').innerText = '';
    document.getElementById('modal-pac-estado').innerText = '';
    crfDataContainer.innerHTML = '<p>Cargando datos de estudio...</p>';
    modalPaciente.style.display = 'block';

    try {
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

            for (const crf of paciente.crfs) {
                html += `<div class="card" style="margin-top: 1rem; background-color: var(--color-fondo);">
                           <div class="card-header" style="padding: 0.75rem 1rem;">
                             <strong>CRF ID: ${crf.idCrf}</strong>
                             <a href="/crf/editar/${crf.idCrf}" class="btn-secundario btn-sm">Editar CRF</a>
                           </div>
                           <div class="card-body" style="padding: 1rem;">`;

                if (crf.datosCrfList && crf.datosCrfList.length > 0) {

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
            crfDataContainer.innerHTML = html;

        } else {
            crfDataContainer.innerHTML = '<p>Este paciente no tiene ningún CRF asociado.</p>';
        }

    } catch (error) {
        document.getElementById('modal-patient-id').innerText = 'Error al cargar los datos';
        crfDataContainer.innerHTML = '<p style="color: var(--color-error);">Error al cargar datos de estudio.</p>';
        console.error("Error en verPaciente:", error);
    }
}

function cerrarModal() {
    if (modalPaciente) {
        modalPaciente.style.display = 'none';
    }
}

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
        const response = await fetch(`/crfs/api/crf/${crfId}`);
        if (!response.ok) {
            throw new Error(`CRF no encontrado (ID: ${crfId}), Estado: ${response.status}`);
        }
        const crf = await response.json();

        document.getElementById('modal-crf-titulo').innerText = `Detalles del CRF: ${crf.idCrf || 'N/A'}`;
        document.getElementById('modal-crf-id').innerText = crf.idCrf || 'N/A';

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

/* --- LÓGICA DEL SELECTOR DE TEMAS --- */

const themeDropdown = document.getElementById('theme-dropdown');

function toggleThemeMenu(event) {
    if (event) event.stopPropagation(); 

    const notifDropdown = document.getElementById('notif-dropdown');
    if (notifDropdown && notifDropdown.classList.contains('show')) {
        notifDropdown.classList.remove('show');
    }

    if (themeDropdown) {
        themeDropdown.classList.toggle('show');
        updateActiveThemeUI();
    }
}

function selectTheme(themeName) {
    if (themeName === 'default') {
        document.documentElement.removeAttribute('data-theme');
    } else {
        document.documentElement.setAttribute('data-theme', themeName);
    }
    localStorage.setItem('appTheme', themeName);
    updateActiveThemeUI();
}

function updateActiveThemeUI() {
    const currentTheme = localStorage.getItem('appTheme') || 'default';
    document.querySelectorAll('.theme-option').forEach(el => el.classList.remove('active'));
    const activeEl = document.getElementById('theme-opt-' + currentTheme);
    if (activeEl) {
        activeEl.classList.add('active');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    updateActiveThemeUI();
});

window.addEventListener('click', function (e) {
    if (!e.target.closest('.theme-wrapper')) {
        if (themeDropdown && themeDropdown.classList.contains('show')) {
            themeDropdown.classList.remove('show');
        }
    }

    if (!e.target.closest('.notification-wrapper')) {
        const notifDropdown = document.getElementById('notif-dropdown');
        if (notifDropdown && notifDropdown.classList.contains('show')) {
            notifDropdown.classList.remove('show');
        }
    }
});

/* --- Menu de Perfil --- */

function toggleProfileMenu(event) {
    event.stopPropagation();
    
    const menu = document.getElementById('profile-dropdown');
    
    if (menu) {
        menu.classList.toggle('show');
    }

    const notifDropdown = document.getElementById('notif-dropdown');
    if (notifDropdown && notifDropdown.classList.contains('show')) {
        notifDropdown.classList.remove('show');
    }
}

window.addEventListener('click', function(e) {
    const menu = document.getElementById('profile-dropdown');
    const trigger = document.querySelector('.profile-trigger');

    if (menu && menu.classList.contains('show')) {
        if (!menu.contains(e.target) && !trigger.contains(e.target)) {
            menu.classList.remove('show');
        }
    }
});


