/**
 * RENIEC DNI Auto-completar
 * Integración con API de RENIEC de Perú para autocompletar nombres y apellidos
 * 
 * Uso:
 * 1. Incluir este script en tu HTML
 * 2. Llamar a initReniecAutocomplete() con la configuración de campos
 * 
 * Ejemplo:
 * initReniecAutocomplete({
 *     dniInputId: 'dni',
 *     nombresInputId: 'nombre',
 *     apellidoPaternoInputId: 'apellidoPaterno',
 *     apellidoMaternoInputId: 'apellidoMaterno',
 *     submitButtonId: 'submitBtn' // Opcional
 * });
 */

(function() {
    'use strict';

    // Configuración global
    const RENIEC_API_URL = '/api/reniec/dni';
    const DNI_LENGTH = 8;

    /**
     * Inicializa el autocompletado de RENIEC en un formulario
     * @param {Object} config - Configuración de IDs de campos
     */
    window.initReniecAutocomplete = function(config) {
        console.log('🚀 Inicializando autocompletado RENIEC', config);

        // Validar configuración
        if (!config || !config.dniInputId) {
            console.error('❌ Error: Se requiere configuración con dniInputId');
            return;
        }

        const dniInput = document.getElementById(config.dniInputId);
        if (!dniInput) {
            console.error('❌ Error: No se encontró el campo DNI con ID:', config.dniInputId);
            return;
        }

        // Estado del componente (usar objeto para mantener la referencia)
        const state = {
            isLoading: false,
            lastSearchedDNI: null
        };

        // Crear indicador de carga si no existe
        let loadingIndicator = createLoadingIndicator(dniInput);

        // Eventos del input DNI
        dniInput.addEventListener('input', function(e) {
            // Solo permitir números
            this.value = this.value.replace(/\D/g, '');
            
            // Limitar a 8 dígitos
            if (this.value.length > DNI_LENGTH) {
                this.value = this.value.slice(0, DNI_LENGTH);
            }

            // Limpiar campos si el DNI es modificado
            if (this.value.length < DNI_LENGTH && state.lastSearchedDNI !== this.value) {
                clearFields(config);
                state.lastSearchedDNI = null;
            }
        });

        dniInput.addEventListener('blur', function() {
            const dni = this.value.trim();
            
            // Solo consultar si tiene 8 dígitos y no se ha consultado antes
            if (dni.length === DNI_LENGTH && dni !== state.lastSearchedDNI) {
                consultarDNI(dni, config, loadingIndicator, state);
            }
        });

        // También permitir consultar con Enter
        dniInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                const dni = this.value.trim();
                
                if (dni.length === DNI_LENGTH) {
                    consultarDNI(dni, config, loadingIndicator, state);
                } else {
                    showNotification('⚠️ El DNI debe tener 8 dígitos', 'warning');
                }
            }
        });

        // Agregar botón de búsqueda opcional
        if (config.addSearchButton) {
            addSearchButton(dniInput, config, loadingIndicator, state);
        }

        console.log('✅ Autocompletado RENIEC inicializado correctamente');
    };

    /**
     * Consulta el DNI en la API de RENIEC
     */
    async function consultarDNI(dni, config, loadingIndicator, state) {
        console.log('🔍 Consultando DNI:', dni);

        try {
            // Mostrar loading
            showLoading(loadingIndicator, true);
            disableFields(config, true);

            // Obtener CSRF token si existe
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

            const headers = {
                'Content-Type': 'application/json'
            };

            if (csrfToken && csrfHeader) {
                headers[csrfHeader] = csrfToken;
            }

            // Hacer la petición
            const response = await fetch(`${RENIEC_API_URL}/${dni}`, {
                method: 'GET',
                headers: headers
            });

            const data = await response.json();

            console.log('📥 Respuesta recibida:', data);

            if (response.ok && data.success && data.data) {
                // Autocompletar campos
                fillFields(config, data.data);
                showNotification('✅ Datos cargados desde RENIEC', 'success');
                state.lastSearchedDNI = dni;
            } else {
                // Error: DNI no encontrado o inválido
                const message = data.message || 'DNI no encontrado en RENIEC';
                showNotification('❌ ' + message, 'error');
                clearFields(config);
            }

        } catch (error) {
            console.error('❌ Error al consultar DNI:', error);
            showNotification('❌ Error al consultar RENIEC. Intenta nuevamente.', 'error');
            clearFields(config);

        } finally {
            showLoading(loadingIndicator, false);
            disableFields(config, false);
        }
    }

    /**
     * Rellena los campos del formulario con los datos de RENIEC
     */
    function fillFields(config, data) {
        console.log('📝 Rellenando campos con:', data);

        if (config.nombresInputId) {
            const nombresInput = document.getElementById(config.nombresInputId);
            if (nombresInput) {
                // La API puede devolver 'nombres' o 'nombre_completo'
                nombresInput.value = data.nombres || data.nombre || '';
                nombresInput.dispatchEvent(new Event('input', { bubbles: true }));
            }
        }

        if (config.apellidoPaternoInputId) {
            const apPaternoInput = document.getElementById(config.apellidoPaternoInputId);
            if (apPaternoInput) {
                // La API devuelve con guión bajo: apellido_paterno
                apPaternoInput.value = data.apellido_paterno || data.apellidoPaterno || '';
                apPaternoInput.dispatchEvent(new Event('input', { bubbles: true }));
            }
        }

        if (config.apellidoMaternoInputId) {
            const apMaternoInput = document.getElementById(config.apellidoMaternoInputId);
            if (apMaternoInput) {
                // La API devuelve con guión bajo: apellido_materno
                apMaternoInput.value = data.apellido_materno || data.apellidoMaterno || '';
                apMaternoInput.dispatchEvent(new Event('input', { bubbles: true }));
            }
        }

        // Marcar campos como autocompletados (útil para validación)
        markAsAutocompleted(config, true);
    }

    /**
     * Limpia todos los campos del formulario
     */
    function clearFields(config) {
        if (config.nombresInputId) {
            const input = document.getElementById(config.nombresInputId);
            if (input) input.value = '';
        }

        if (config.apellidoPaternoInputId) {
            const input = document.getElementById(config.apellidoPaternoInputId);
            if (input) input.value = '';
        }

        if (config.apellidoMaternoInputId) {
            const input = document.getElementById(config.apellidoMaternoInputId);
            if (input) input.value = '';
        }

        markAsAutocompleted(config, false);
    }

    /**
     * Deshabilita/habilita campos durante la carga
     */
    function disableFields(config, disable) {
        const fieldsToDisable = [
            config.nombresInputId,
            config.apellidoPaternoInputId,
            config.apellidoMaternoInputId,
            config.submitButtonId
        ];

        fieldsToDisable.forEach(fieldId => {
            if (fieldId) {
                const field = document.getElementById(fieldId);
                if (field) {
                    field.disabled = disable;
                }
            }
        });
    }

    /**
     * Marca campos como autocompletados
     */
    function markAsAutocompleted(config, isAutocompleted) {
        const fields = [
            config.nombresInputId,
            config.apellidoPaternoInputId,
            config.apellidoMaternoInputId
        ];

        fields.forEach(fieldId => {
            if (fieldId) {
                const field = document.getElementById(fieldId);
                if (field) {
                    if (isAutocompleted) {
                        field.classList.add('reniec-autocompleted');
                        field.setAttribute('data-reniec-verified', 'true');
                    } else {
                        field.classList.remove('reniec-autocompleted');
                        field.removeAttribute('data-reniec-verified');
                    }
                }
            }
        });
    }

    /**
     * Crea el indicador de carga visual
     */
    function createLoadingIndicator(dniInput) {
        // Buscar si ya existe
        let indicator = dniInput.parentElement.querySelector('.reniec-loading-indicator');
        
        if (!indicator) {
            indicator = document.createElement('span');
            indicator.className = 'reniec-loading-indicator';
            indicator.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Consultando...';
            indicator.style.cssText = 'display: none; margin-left: 10px; color: #0066cc; font-size: 14px;';
            dniInput.parentElement.appendChild(indicator);
        }

        return indicator;
    }

    /**
     * Muestra/oculta el indicador de carga
     */
    function showLoading(indicator, show) {
        if (indicator) {
            indicator.style.display = show ? 'inline-block' : 'none';
        }
    }

    /**
     * Agrega un botón de búsqueda al lado del input DNI
     */
    function addSearchButton(dniInput, config, loadingIndicator, state) {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'btn btn-sm btn-primary reniec-search-btn';
        button.innerHTML = '<i class="fas fa-search"></i>';
        button.title = 'Buscar en RENIEC';
        button.style.cssText = 'margin-left: 5px;';

        button.addEventListener('click', function() {
            const dni = dniInput.value.trim();
            if (dni.length === DNI_LENGTH) {
                consultarDNI(dni, config, loadingIndicator, state);
            } else {
                showNotification('⚠️ El DNI debe tener 8 dígitos', 'warning');
            }
        });

        dniInput.parentElement.appendChild(button);
    }

    /**
     * Muestra notificación al usuario
     */
    function showNotification(message, type = 'info') {
        console.log(`[${type.toUpperCase()}] ${message}`);

        // Usar el sistema de notificaciones del proyecto si existe
        if (typeof window.showToast === 'function') {
            window.showToast(message, type);
            return;
        }

        // Fallback: alert simple
        if (type === 'error' || type === 'warning') {
            // Solo mostrar alert para errores importantes
            // alert(message);
        }

        // También se puede implementar con un toast personalizado
        createToast(message, type);
    }

    /**
     * Crea un toast personalizado simple
     */
    function createToast(message, type) {
        // Eliminar toast previo si existe
        const existingToast = document.querySelector('.reniec-toast');
        if (existingToast) {
            existingToast.remove();
        }

        const toast = document.createElement('div');
        toast.className = `reniec-toast reniec-toast-${type}`;
        toast.textContent = message;
        
        // Estilos inline para el toast
        toast.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 15px 20px;
            background: ${type === 'success' ? '#28a745' : type === 'error' ? '#dc3545' : type === 'warning' ? '#ffc107' : '#17a2b8'};
            color: white;
            border-radius: 5px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            z-index: 10000;
            animation: slideIn 0.3s ease-out;
            font-size: 14px;
            max-width: 350px;
        `;

        document.body.appendChild(toast);

        // Auto-eliminar después de 4 segundos
        setTimeout(() => {
            toast.style.animation = 'slideOut 0.3s ease-in';
            setTimeout(() => toast.remove(), 300);
        }, 4000);
    }

    // Agregar estilos CSS para animaciones y clases
    if (!document.getElementById('reniec-styles')) {
        const style = document.createElement('style');
        style.id = 'reniec-styles';
        style.textContent = `
            @keyframes slideIn {
                from { transform: translateX(100%); opacity: 0; }
                to { transform: translateX(0); opacity: 1; }
            }
            
            @keyframes slideOut {
                from { transform: translateX(0); opacity: 1; }
                to { transform: translateX(100%); opacity: 0; }
            }

            .reniec-autocompleted {
                background-color: #e8f5e9 !important;
                border-color: #28a745 !important;
            }

            .reniec-autocompleted:focus {
                box-shadow: 0 0 0 0.2rem rgba(40, 167, 69, 0.25) !important;
            }

            .reniec-loading-indicator {
                transition: opacity 0.3s ease;
            }
        `;
        document.head.appendChild(style);
    }

    console.log('✅ Script RENIEC DNI Autocomplete cargado');
})();
