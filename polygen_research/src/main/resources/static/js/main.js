/* --- Lógica de Responsividad (Menú Lateral) --- */
// Función para mostrar/ocultar la barra lateral en pantallas pequeñas.
function toggleMenu() {
    const sidebar = document.getElementById('sidebar');
    if (sidebar) { // Verifica si el elemento existe antes de usarlo
        sidebar.classList.toggle('open');
    } else {
        console.error("Elemento con ID 'sidebar' no encontrado.");
    }
}

/* --- Lógica para Marcar el Enlace Activo en el Menú --- */
// Selecciona todos los enlaces de navegación en la barra lateral.
const navLinks = document.querySelectorAll('#sidebar nav a'); // Más específico
const currentPath = window.location.pathname; // Obtiene la URL actual

navLinks.forEach(link => {
    const linkHref = link.getAttribute('href');
    // Comprueba si el href del enlace coincide exactamente con la ruta actual
    // O si la ruta actual comienza con el href del enlace (para subpáginas)
    // Se asegura que el link no sea solo "/" para evitar marcarlo siempre.
    if (linkHref && (linkHref === currentPath || (linkHref !== '/' && currentPath.startsWith(linkHref)))) {
        link.classList.add('active'); // Añade la clase 'active' para resaltarlo (usando el CSS)
    }

    // Añade un listener para cerrar el menú en móvil al hacer clic en un enlace
    link.addEventListener('click', (e) => {
        if (window.innerWidth <= 768) {
            const sidebar = document.getElementById('sidebar');
            if (sidebar) {
                sidebar.classList.remove('open');
            }
        }
        // IMPORTANTE: No se usa e.preventDefault(), se deja que el enlace (th:href) funcione.
    });
});

/* --- Lógica para Modales (Pop-ups) --- */

// Obtiene las referencias a los modales (si existen en la página actual)
const modalPaciente = document.getElementById('modal-ver-paciente');
const modalCrf = document.getElementById('modal-ver-crf');

// Función asíncrona para mostrar detalles de un Paciente
// Función asíncrona para mostrar detalles de un Paciente
async function verPaciente(pacienteId) {
    if (!modalPaciente) {
        console.error("El modal 'modal-ver-paciente' no se encuentra en esta página.");
        return;
    }
    
    // Referencia al nuevo contenedor de datos CRF
    const crfDataContainer = document.getElementById('modal-pac-crf-data');

    // Muestra el modal y un mensaje de carga
    document.getElementById('modal-patient-id').innerText = 'Cargando...';
    // Limpia campos anteriores
    document.getElementById('modal-pac-codigo').innerText = '';
    document.getElementById('modal-pac-nombre').innerText = '';
    document.getElementById('modal-pac-apellido').innerText = '';
    document.getElementById('modal-pac-estado').innerText = '';
    crfDataContainer.innerHTML = '<p>Cargando datos de estudio...</p>'; // Limpia datos CRF
    modalPaciente.style.display = 'block';

    try {
        // Llama a la API (esto ya lo tenías)
        const response = await fetch(`/pacientes/api/paciente/${pacienteId}`);
        if (!response.ok) {
            throw new Error(`Paciente no encontrado (ID: ${pacienteId}), Estado: ${response.status}`);
        }
        const paciente = await response.json(); 

        // Rellena el modal con los datos personales (esto ya lo tenías)
        document.getElementById('modal-patient-id').innerText = `Detalles del Paciente: ${paciente.codigoPaciente || 'N/A'}`;
        document.getElementById('modal-pac-codigo').innerText = paciente.codigoPaciente || 'N/A';
        document.getElementById('modal-pac-nombre').innerText = paciente.nombre || 'N/A';
        document.getElementById('modal-pac-apellido').innerText = paciente.apellido || 'N/A';
        document.getElementById('modal-pac-estado').innerText = paciente.estado || 'N/A';

        // --- ¡NUEVA LÓGICA PARA DATOS DE ESTUDIO! ---
        
        // 1. Revisa si el paciente tiene CRFs
        if (paciente.crfs && paciente.crfs.length > 0) {
            let html = ''; // Variable para construir el HTML
            
            // 2. Itera sobre cada CRF del paciente
            for (const crf of paciente.crfs) {
                // Añade un título para este CRF
                html += `<div class="card" style="margin-top: 1rem; background-color: var(--color-fondo);">
                           <div class="card-header" style="padding: 0.75rem 1rem;">
                             <strong>CRF ID: ${crf.idCrf}</strong>
                             <a href="/crf/editar/${crf.idCrf}" class="btn-secundario btn-sm">Editar CRF</a>
                           </div>
                           <div class="card-body" style="padding: 1rem;">`;
                
                // 3. Revisa si este CRF tiene datos dinámicos
                if (crf.datosCrfList && crf.datosCrfList.length > 0) {
                    
                    // 4. Itera sobre cada dato (pregunta y respuesta)
                    for (const dato of crf.datosCrfList) {
                        html += `<div class="patient-data-grid" style="gap: 0.5rem 1rem;">
                                   <strong>${dato.campoCrf ? dato.campoCrf.nombre : 'Campo desconocido'}:</strong>
                                   <span>${dato.valor || '(Sin respuesta)'}</span>
                                 </div>`;
                    }
                } else {
                    html += '<p>Este CRF no tiene datos registrados.</p>';
                }
                html += '</div></div>'; // Cierra card-body y card
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
    // Muestra el modal y mensaje de carga
    document.getElementById('modal-crf-titulo').innerText = 'Cargando...';
     // Limpia campos anteriores
    document.getElementById('modal-crf-id').innerText = '';
    document.getElementById('modal-crf-paciente').innerText = '';
    document.getElementById('modal-crf-grupo').innerText = '';
    document.getElementById('modal-crf-fecha').innerText = '';
    document.getElementById('modal-crf-estado').innerText = '';
    document.getElementById('modal-crf-obs').innerText = '';
    modalCrf.style.display = 'block';

    try {
        // Realiza petición fetch al endpoint del backend para CRF
        // NOTA: Necesitas crear este endpoint @GetMapping("/crfs/api/crf/{id}") en tu CrfController que devuelva JSON.
        const response = await fetch(`/crfs/api/crf/${crfId}`);
        if (!response.ok) {
             throw new Error(`CRF no encontrado (ID: ${crfId}), Estado: ${response.status}`);
        }
        const crf = await response.json(); // Convierte la respuesta JSON

        // Rellena el modal con los datos del CRF
        document.getElementById('modal-crf-titulo').innerText = `Detalles del CRF: ${crf.idCrf || 'N/A'}`;
        document.getElementById('modal-crf-id').innerText = crf.idCrf || 'N/A';
        // Accede a los datos del paciente anidado (si el backend los incluye)
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
window.onclick = function(event) {
    if (event.target == modalPaciente) {
        cerrarModal();
    }
    if (event.target == modalCrf) {
        cerrarModalCrf();
    }
}

/* --- Lógica de Personalización (Modo Oscuro) --- */
const darkModeToggle = document.getElementById('dark-mode-toggle');

if (darkModeToggle) {
    // Listener para el cambio en el interruptor
    darkModeToggle.addEventListener('change', () => {
        // --- ¡CAMBIO AQUÍ! ---
        // Ahora aplicamos/quitamos la clase en documentElement (<html>)
        document.documentElement.classList.toggle('dark-mode');

        // Guardar preferencia en localStorage (revisando documentElement)
        if (document.documentElement.classList.contains('dark-mode')) {
            localStorage.setItem('theme', 'dark');
        } else {
            localStorage.setItem('theme', 'light');
        }
    });

    if (localStorage.getItem('theme') === 'dark') {
        // Comprobamos si el interruptor existe antes de marcarlo
        if (darkModeToggle) {
             darkModeToggle.checked = true;
        }
    }
}

/* --- Lógica de Personalización (Color Primario - Ejemplo) --- */
// Función simple para cambiar la variable CSS --color-primario.
// Podrías llamarla desde botones o un selector de color.
function cambiarColorPrimario(color) {
    if (color) {
        document.documentElement.style.setProperty('--color-primario', color);
        // Opcional: Guardar preferencia en localStorage
        localStorage.setItem('primaryColor', color);
    }
}

// Opcional: Cargar color primario guardado al iniciar
const savedColor = localStorage.getItem('primaryColor');
if (savedColor) {
    cambiarColorPrimario(savedColor);
}


/* --- FUNCIONES SIMULADAS ELIMINADAS --- */
// - simularLogin: No necesaria, Spring Security maneja el login.
// - ingresarDatos: No necesaria, los formularios HTML (th:action) envían los datos.
// - simularExportar: Reemplazada por enlaces directos a endpoints del backend.
// - mostrarNotificaciones: Reemplazada por mensajes flash de Thymeleaf/Spring.
// - iniciarAutoguardado: Eliminada por complejidad, implementar si es necesario más adelante.