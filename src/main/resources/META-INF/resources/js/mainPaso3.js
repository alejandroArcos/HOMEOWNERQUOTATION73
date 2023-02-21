$(document).ready(function() {
	console.log("READY PASO 3");
	validaErrorCotizacion();
	compruebaBqOri();
	validaModo();
	setModaltitulo();
	setBase64();
	validaTipoMoneda();
});

function validaErrorCotizacion(){
	console.log("modo cotizacion: " + infCotiJson.modo);
	if(infCotiJson.modo == modo.ERROR.toString()){
		console.log("REDIRECCIONAR AL PASO 1");
		infCotiJson.modo = modo.EDICION;
		regresaPaso1();
	}
}

function setModaltitulo(){
	if(infCotiJson.modo.includes("ENDOSO")){
		$("#titModalEmisionp3").text("El endoso se ha generado satisfactoriamente");
	}else{
		$("#titModalEmisionp3").text("Póliza generada exitosamente");
	}
}

function regresaPaso1(){
	showLoader();
	actualizainfoCot();
	var strInfCotiJson = JSON.stringify(infCotiJson); 
	console.log("infCotiJson: ");
	console.log(strInfCotiJson);
	$.post( redirigeURL, {
		/*infoCot : JSON.stringify( infoCotJson ),*/
		infoCot : JSON.stringify(infCotiJson),
		paso : seleccionaVentana()
	} ).done( function(data) {
		var response = JSON.parse( data );
		if (response.code == 0) {
			window.location.href = response.msg;
		} else {
			showMessageError( '.navbar', response.msg, 0 );
			hideLoader();
		}
	} );
}

function validaTipoMoneda() {
	$.each( $( ".moneda" ), function(key, registro) {
		daFormatoMoneda( registro );
	} );
}

/*********************************************************************************************************************/
/*****************************************************PASO 3 POST*****************************************************/
$("#paso3_slip").click(function(e) {
    resetSession();
    showLoader();
    e.preventDefault();
    $.post($("#getSlip").val(), {
        cotizacion: infCotiJson.cotizacion,
        version: infCotiJson.version,
        word: 0
    }).done(function(data) {

        var respuestaJson = JSON.parse(data);
        if (respuestaJson.code == 0) {
            var buffer = new Uint8Array(respuestaJson.documento);
            var blob = new Blob([buffer], { type: "application/pdf" });
            $("#aPdf").attr("href", window.URL.createObjectURL(blob));
            var link = document.getElementById("aPdf");
            link.download = respuestaJson.nombre + respuestaJson.extension;
            link.click();
            validaBotonEmision(respuestaJson.estado);
            
            $("#btnEmitEndoso").prop("disabled", false);
            hideLoader();
            
            if(perfilSuscriptor == 1 ) {
            	$("#btnEnvCotiSusAgente").prop("disabled", false);
            	$("#btnEnvCotiSusAgente").removeClass('d-none');
            }
            else {
            	$.post(validaAgenteURL, {
            		cotizacion: infCotiJson.cotizacion,
        	    	codigoAgente: ''
        	    }).done(function(data) {
        	    	
        	    	var response = JSON.parse(data);
        	    	
        	    	if(response.code != 0) {
        	    		if(response.code == 3) {
        	    			$("#modalBloqueoAgente").modal('show');
        	    			$("#paso3_next").prop("disabled", true);
        	    		}
        	    		else {
        	    			showMessageError('.navbar', response.msg, 0);
        	    		}
        	    	}
        	    	else {
        	    		
        	    		$("#paso3_next").prop("disabled", false);
        				$("#paso3_next").removeClass('d-none');
        	    	}
        	    });
            }
        } else {
            /*agregaAlertError("Mensaje: " + respuestaJson.msg);*/
        	showMessageError(".navbar", "Mensaje: " + respuestaJson.msg, 1);
            hideLoader();
        }
    });
});

$('#btnFacturaSuscrip').click(function() {
    var idFac = ($('#chkfactauto').is(':checked')) ? 1 : 0;
    $('#txtBtnEmiteFactu').val(idFac);
    showLoader();

    $.post(getEmisionArt492Url, {
        cotizacion: infCotiJson.cotizacion,
        version: infCotiJson.version,
        factura: idFac,
        cotizador: seleccionaVentana()
    }).done(function(data) {
        var response = jQuery.parseJSON(data);
        console.log('aquiiii');
        console.log(response);
        if (response.code == 0) {
            llenaInfoModalPoliza(response);
            $('#modalGenerarPoliza').modal({
                show: true
            });
        } else if (response.code == 4) {
            llenaInfoModalPoliza(response);
            $('#modalGenerarPoliza').modal({
                show: true
            });
            showMessageError('#modalGenerarPoliza', (response.msg), 0);
        } else {
            showMessageError('.navbar', (response.msg), 0);
        }
    }).always(function() {
        hideLoader();
    });
});

async function adjuntaArchivos() {

    var url = new URL(window.location.href);
    var data = new FormData();
    var auxiliarDoc = '{';

    $.each($('#docAgenSusc')[0].files, function(i, file) {
        data.append('file-' + i, file);
        var nomAux = file.name.split('.');
        if (i == 0) {
            auxiliarDoc += '\"file-' + i + '\" : {';
        } else {
            auxiliarDoc += ', \"file-' + i + '\" : {';
        }
        auxiliarDoc += '\"nom\" : \"' + nomAux[0] + '\",';
        auxiliarDoc += '\"ext\" : \"' + nomAux[1] + '\"}';
    });
    auxiliarDoc += '}';

    data.append('isRevire', $('#txtAuxEnvDoc').val());
    data.append('auxiliarDoc', auxiliarDoc);
    data.append('comentarios', $('#comentariosDosSuscrip').val());
    data.append('folio', infCotiJson.folio);
    data.append('cotizacion', infCotiJson.cotizacion);
    data.append('version', infCotiJson.version);
    data.append('modo', infCotiJson.modo);
    data.append('url', url.origin + url.pathname);
    data.append('totArchivos', $('#docAgenSusc')[0].files.length);
    data.append('tipoCotizacion', infCotiJson.tipoCotizacion);
	data.append('pantalla', infCotiJson.pantalla);

    $.ajax({
        url: $('#txtSendMailAgenteSuscriptor').val(),
        data: data,
        processData: false,
        contentType: false,
        type: 'POST',
        success: function(data) {
            if (data != "") {
                var response = jQuery.parseJSON(data);
                if (response.codigo > 0) {
                    $('#fileModal').modal('hide');
                    hideLoader();
                    showMessageError('.navbar', response.error, 0);
                } else {
                	goToHome();
                }
            } else {
            	goToHome();
            }
        }
    });
}

$('#modalGenerarPoliza').on('hidden.bs.modal', function() {
    window.scrollTo(0, 0);
    goToHome();
    /*goToPage(seleccionaVentana());*/
    /*var url = new URL(window.location.href);
    window.location.href = url.origin + url.pathname;*/
});
/*****************************************************PASO 3 POST*****************************************************/
/*****************************************************PASO 3 EMISION**************************************************/
$("#paso3_emision").on('click', function(e) {
    consumeEmisionData();
});

/*
$("#paso3_next").click(function(e){
	
	showLoader();
    $.post($('#txtJSGetEmisionData').val(), {
        cotizacion: infCotiJson.cotizacion,
        version: infCotiJson.version
    }).done(function(data) {
        sessionExtend();
        var response = jQuery.parseJSON(data);
        if (response.code == 0) {
        	/*
            varAuxiliares.tipoPersona = isFisica_Moral(response.datosCliente.tipoPer);
            preCargaDatos(response);
            */
			/*
            showLoader();
			actualizainfoCot();
			$.post( redirigeURL, {
				infoCot : JSON.stringify( infCotiJson ),
				paso : enlace.PASO4
				/*paso : seleccionaVentana()*/
				/*paso : enlace.PASO2*/
			/*
			} ).done( function(data) {
				var response = JSON.parse( data );
				if (response.code == 0) {
					$("#infoCotizacion").val(response.msg);
					$("#paso3-form").submit();
				} else {
					showMessageError( '.navbar', response.msg, 0 );
					hideLoader();
				}
			} );
            /*hideLoader();*/
		/*
        } else {
            showMessageError('.navbar', 'Error al consultar la información', 0);
            hideLoader();
        }
        /*
        if (valIsNullOrEmpty($('#txtBtnEmiteFactu').val())) {
            validaPrimaMax492(0);
        }
        */
	/*
    }).fail(function(e) {
        showMessageError('.navbar', 'Error al consultar la información (data)', 0);
        hideLoader();
    });
	
});
*/

$('#btnNoAcepPropuesta').click(function() {
    $('#modalRechazarProp').modal('show');
});

$('#btnEnvRecha').click(function(e) {
    showLoader();
    e.preventDefault();
    var errores = false;
    errores = (noSelect($('#mdlRechOp')) ? true : errores);
    errores = (valIsNullOrEmpty($('#comentariosRechazarProp').val().trim()) ? true : errores);
    if (errores) {
        showMessageError('#modalRechazarProp .modal-header', 'Los campos son obligatorios', 0);
        hideLoader();
    } else {
        showLoader();
        var url = new URL(window.location.href);
        var motivo = $('#modalRechazarProp .modal-body select').val();
        $.post($('#txtRechazaCotizacionURL').val(), {
            cotizacion: infCotiJson.cotizacion,
            version: infCotiJson.version,
            motivoRechazo: motivo,
            motivo: $('#comentariosRechazarProp').val(),
			pantalla: infCotiJson.pantalla
        }).done(function() {
        	goToHome();
        });
    }
});
/*****************************************************PASO 3 EMISION**************************************************/
/*********************************************************************************************************************/
/***************************************************Funciones Botones*************************************************/
$('#btnRecotizar').click(function() {
    $('#btnSuscripEnvSus2').removeAttr('hidden');
    $('#btnSuscripEnvSus').prop('hidden', true);
    $('#fileModal').modal('show');
});

$('#btnSuscripEnvSus').click(function() {
    showLoader();
    $('#comentariosDosSuscrip').trigger('keyup');
    $('#txtAuxEnvDoc').val('0');
    adjuntaArchivos();
});

$('#btnSuscripEnvSus2').click(function() {
    showLoader();
    $('#comentariosDosSuscrip').trigger('keyup');

    if ($('#docAgenSusc')[0].files.length == 0 && $('#comentariosDosSuscrip').val().trim().length == 0) {
        showMessageError('#fileModal .modal-header', 'Agregar documentos y/o comentarios', 0);
        hideLoader();
    } else {
        $('#txtAuxEnvDoc').val('1');
        adjuntaArchivos();
    }
});

$('#btnCederComision').click(function(e) {
    try {
        showLoader();
        if (valIsNullOrEmpty($('#txtCederComision').val())) {
            showMessageError('.navbar', 'Sin comisiòn ', 0);
        } else {
            $.post($('#txtJSGetSeccionComisionUrl').val(), {
                seccomi: $('#txtCederComision').val(),
                tipoCoti : infCotiJson.tipoCotizacion.toString(),
                cotizacion: infCotiJson.cotizacion,
                version: infCotiJson.version
            }).done(function(data) {
                sessionExtend();
                var respuestaJson = JSON.parse(data);
                if (respuestaJson.code == 0) {
                    showMessageSuccess('.navbar', 'Información actualizada correctamente', 0);
                } else {
                    showMessageError('.navbar', respuestaJson.msg, 0);
                }
            }).fail(function() {
                showMessageError('.navbar', 'Error al consultar la información', 0);
                hideLoader();
            });
        }
        hideLoader();
    } catch (err) {

        hideLoader();
        showMessageError('.navbar', 'Error al consultar la información', 0);
    }
});


$('#btnRecalcularPrima').click(function(e) {
    try {
        showLoader();
        var primaNeta = parseFloat(valIsNullOrEmpty($('#primaNeta').text()) ?
            0 : $('#primaNeta').text().replace(/[$,]/g, ''));
        var primaObj = parseFloat(valIsNullOrEmpty($('#txtPrimaObj').val()) ?
            0 : $('#txtPrimaObj').val().replace(/[$,]/g, ''));
		var gastos = parseFloat(valIsNullOrEmpty($('#gastos').val()) ?
            0 : $('#gastos').val().replace(/[$,]/g, ''));
		var recargoPago = parseFloat(valIsNullOrEmpty($('#recargoPago').val()) ?
            0 : $('#recargoPago').val().replace(/[$,]/g, ''));
        var idPerfil = parseInt($('#txtIdPerfilUser').val());
        var minPrima = parseFloat($('#txtMinPrima').val());
        var cambioDoll = parseFloat($('#txtTpoCambio').val());
        /*var tipoMonSelect = parseInt($('#dc_moneda option:selected').val());   CAMBIAR POR NUEVA VARIABLE*/ 
        var tipoMonSelect = parseInt($('#dc_moneda').val());
        if (tipoMonSelect != 1) {
            minPrima = minPrima / cambioDoll;
        }

        var nuevaCaratula = true;
        /*
		if (idPerfil != 1) {
            if (primaObj < primaNeta) {
                nuevaCaratula = false;
                showMessageError('.navbar', 'La prima capturada no puede ser menor a ' + generaFormatoNumerico(primaNeta, true, true, false), 0);
            }
        }
		*/
        
		if (primaObj < minPrima && primaObj != 0) {
            nuevaCaratula = false;
            showMessageError('.navbar', 'La prima minima para el Cotizador '+ infCotiJson.tipoCotizacion +' es ' + generaFormatoNumerico(minPrima, true, true, false), 1);
        }

		if((perfilSuscriptor != 1 && perfilJapones != 1) || banderaEditar == false){
			gastos = -1;
			recargoPago = -1;
		}
		
        if (nuevaCaratula) {
        	
        	$.post(validaDescuentoURL, {
                cotizacion: infCotiJson.cotizacion,
                version: infCotiJson.version,
				primaNueva: primaObj
            }).done(function(dataPrima) {
            	
            	var responseJson = JSON.parse(dataPrima);
            	
            	if(responseJson.code == 0) {
        	
		            $.post(recalculoPrimaURL, {
		                cotizacion: infCotiJson.cotizacion,
		                version: infCotiJson.version,
						pantalla: infCotiJson.pantalla,
		                primaObjetivo: primaObj,
						gastos: gastos,
						recargoPago: recargoPago
		            }).done(function(data) {
		
		
		                var respuestaJson = JSON.parse(data);
		                if (respuestaJson.code == 0) {
		                    $('#txtEmailAgente').val(respuestaJson.email);
		                    var band = null;
		                    $('#tabPaso3').html("");
		                    $.each(respuestaJson.datosCaratula, function(k, valCaratula) {
		                        if (!(valCaratula.contenedor == band)) {
		                            band = valCaratula.contenedor;
		
									if(perfilSuscriptor == 1 || perfilJapones == 1) {
										$('#tabPaso3').append("<tr><th>" + band + "</td><td></td><td></td><td></th></tr>");
									}
									else{
										$('#tabPaso3').append("<tr><th>" + band + "</td><td></td><td></th></tr>");
									}
		                            
		                        }
		
								if(perfilSuscriptor == 1 || perfilJapones == 1){
									$('#tabPaso3').append("<tr><td>" + valCaratula.titulo + "</td><td class=\"number\">" + valCaratula.sa + "</td><td class=\"number\">" + valCaratula.prima + "</td><td>" + valCaratula.deducible + "</td></tr>");	
								}
								else{
									$('#tabPaso3').append("<tr><td>" + valCaratula.titulo + "</td><td class=\"number\">" + valCaratula.sa + "</td><td>" + valCaratula.deducible + "</td></tr>");
								}
		                        
		                    });
		
		                    $('#tabPaso3_2').html("<tr><td>Prima Neta:</td><td id='primaNeta' class=\"number\">" + setCoinFormat('' + respuestaJson.primaNeta) + "</td></tr>");                    
		
							if(perfilSuscriptor == 1 || perfilJapones == 1) {
								$('#tabPaso3_2').append("<tr><td>Recargo por Pago Fraccionado:</td><td class=\"number\"><input id=\"recargoPago\" class=\"moneda campoEditable\" value=\"" + setCoinFormat('' + respuestaJson.recargo) + "\" disabled=\"true\" /></td><td><a onclick=\"editarCamposPrima('recargoPago')\" style=\"color: #0275d8; text-decoration: underline;\">Editar</a></td></tr>");
		                		$('#tabPaso3_2').append("<tr><td>Gastos de Expedición:</td><td class=\"number\"><input id=\"gastos\" class=\"moneda campoEditable\" value=\"" + setCoinFormat('' + respuestaJson.gastos) + "\" disabled=\"true\" /></td><td><a onclick=\"editarCamposPrima('gastos')\" style=\"color: #0275d8; text-decoration: underline;\">Editar</a></td></tr>");	
							}
							else {
								$('#tabPaso3_2').append("<tr><td>Recargo por Pago Fraccionado:</td><td class=\"number\"><input id=\"recargoPago\" class=\"moneda campoEditable\" value=\"" + setCoinFormat('' + respuestaJson.recargo) + "\" disabled=\"true\" /></td></tr>");
		                    	$('#tabPaso3_2').append("<tr><td>Gastos de Expedición:</td><td class=\"number\"><input id=\"gastos\" class=\"moneda campoEditable\" value=\"" + setCoinFormat('' + respuestaJson.gastos) + "\" disabled=\"true\" /></td></tr>");
							}
		
		                    $('#tabPaso3_2').append("<tr><td>I.V.A.:</td><td class=\"number\">" + setCoinFormat('' + respuestaJson.iva) + "</td></tr>");
		                    $('#tabPaso3_3').html(setCoinFormat('' + respuestaJson.total));
		                    
							var tipoMonSelect = parseInt($('#dc_moneda').val());
		                    if (tipoMonSelect == 1) {
		                        $('#titPrimaObj').text("Prima Objetivo: (Pesos)");
		                    } else {
		                        $('#titPrimaObj').text("Prima Objetivo: (Dolares)");
		                    }
		
		                    hideLoader();
		                    showMessageSuccess('.navbar', 'Información actualizada correctamente', 0);
		
							$("#paso3_slip").attr('disabled', false);
							$("#paso3_next").attr('disabled', true);
							$("#paso3_next").addClass('d-none');
							banderaEditar = false;
		                } else {
		                    showMessageError('.navbar', respuestaJson.msg, 0);
		
		                    hideLoader();
							banderaEditar = false;
		                }
		            });
            	}
            	else {
            		showMessageError('.navbar', responseJson.msg, 0);
                    hideLoader();
                    banderaEditar = false;
            	}
            });
        }
        $('#txtPrimaObj').val('');
        hideLoader();
    } catch (err) {

        hideLoader();
        showMessageError('.navbar', 'Error al consultar la información ' + primaNeta, 0);
    }
});

function editarCamposPrima(campo){
	
	showMessageError('.navbar', "Se debe recalcular prima", 0);
	
	$("#"+campo).attr('disabled', false);
	$("#"+campo).focus();
	
	$("#btnEnvCotiSusAgente").attr('disabled', true);
	$("#btnContinuarJK").attr('disabled', true);
	$("#paso3_slip").attr('disabled', true);
	$("#paso3_next").attr('disabled', true);
	$("#paso3_next").addClass('d-none');
	
	banderaEditar = true;
}

$("#tabPaso3_2").on('blur', '.campoEditable', function(event) {
	
	$(this).attr('disabled', true);
});

$("#tabPaso3_2").on("keyup", '.moneda', function(event){
	$(event.target).val(function(index, value) {
        var aux = value.replace(/[$,]/g, '');
        aux = aux.replace(/\D/g, "")
            .replace(/([0-9])([0-9]{2})$/, '$1.$2')
            .replace(/\B(?=(\d{3})+(?!\d)\.?)/g, ",");
        return '$' + aux;
    });
});

$("#tabPaso3_2" ).on("blur", '.moneda', function(event){
	this.value = this.value.replace(/[^0-9\.]/g,'');
	daFormatoMoneda($(this));
});

/*
$('#btnRecalcularPrima').click(function(e) {
    try {
        showLoader();
        var primaNeta = parseFloat(valIsNullOrEmpty($('#primaNeta').text()) ?
            0 : $('#primaNeta').text().replace(/[$,]/g, ''));
        var primaObj = parseFloat(valIsNullOrEmpty($('#txtPrimaObj').val()) ?
            0 : $('#txtPrimaObj').val().replace(/[$,]/g, ''));
        var idPerfil = parseInt($('#txtIdPerfilUser').val());
        var minPrima = parseFloat($('#txtMinPrima').val());
        var cambioDoll = parseFloat($('#txtTpoCambio').val());
        var tipoMonSelect = parseInt($('#dc_moneda option:selected').val());   CAMBIAR POR NUEVA VARIABLE 
        var tipoMonSelect = parseInt($('#dc_moneda').val());
        if (tipoMonSelect != 1) {
            minPrima = minPrima / cambioDoll;
        }

        var nuevaCaratula = true;
        if (idPerfil != 1) {
            if (primaObj < primaNeta) {
                nuevaCaratula = false;
                showMessageError('.navbar', 'La prima capturada no puede ser menor a ' + generaFormatoNumerico(primaNeta, true, true, false), 0);
            }
        }
        if (primaObj < minPrima) {
            nuevaCaratula = false;
            showMessageError('.navbar', 'La prima minima para el Cotizador '+ infCotiJson.tipoCotizacion +' es ' + generaFormatoNumerico(minPrima, true, true, false), 1);
        }
        if (nuevaCaratula) {
            $.post($("#getCaratulaComision").val(), {
                cotizacion: infCotiJson.cotizacion,
                tipoCoti : infCotiJson.tipoCotizacion.toString(),
                version: infCotiJson.version,
                comision: primaObj
            }).done(function(data) {


                var respuestaJson = JSON.parse(data);
                if (respuestaJson.code == 0) {
                    $('#txtEmailAgente').val(respuestaJson.email);
                    var band = null;
                    $('#tabPaso3').html("");
                    $.each(respuestaJson.datosCaratula, function(k, valCaratula) {
                        if (!(valCaratula.contenedor == band)) {
                            band = valCaratula.contenedor;

                            $('#tabPaso3').append("<tr><th>" + band + "</td><td></td><td></td><td></th></tr>");
                        }
                        $('#tabPaso3').append("<tr><td>" + valCaratula.titulo + "</td><td class=\"number\">" + valCaratula.sa + "</td><td class=\"number\">" + valCaratula.prima + "</td><td>" + valCaratula.deducible + "</td></tr>");
                    });
                    $('#tabPaso3_2').html("<tr><td>Prima Neta:</td><td id='primaNeta' class=\"number\">" + setCoinFormat('' + respuestaJson.primaNeta) + "</td></tr>");
                    $('#tabPaso3_2').append("<tr><td>Recargo por Pago Fraccionado:</td><td class=\"number\">" + setCoinFormat('' + respuestaJson.recargo) + "</td></tr>");
                    $('#tabPaso3_2').append("<tr><td>Gastos de Expedición:</td><td class=\"number\">" + setCoinFormat('' + respuestaJson.gastos) + "</td></tr>");
                    $('#tabPaso3_2').append("<tr><td>I.V.A.:</td><td class=\"number\">" + setCoinFormat('' + respuestaJson.iva) + "</td></tr>");
                    $('#tabPaso3_3').html(setCoinFormat('' + respuestaJson.total));
                    var tipoMonSelect = parseInt($('#dc_moneda option:selected').val());
                    if (tipoMonSelect == 1) {
                        $('#titPrimaObj').text("Prima Objetivo (Pesos):");
                    } else {
                        $('#titPrimaObj').text("Prima Objetivo (Dolares):");
                    }
                    hideLoader();
                    showMessageSuccess('.navbar', 'Información actualizada correctamente', 0);
                } else {
                    agregaAlertError(respuestaJson.msg);

                    hideLoader();
                }
            });
        }
        $('#txtPrimaObj').val('');
        hideLoader();
    } catch (err) {

        hideLoader();
        showMessageError('.navbar', 'Error al consultar la información ' + primaNeta, 0);
    }
});
*/

/*
$("#paso3_back").click(function (){
	goToPage(enlace.PASO2);
});
*/

$("#paso3_back").click(function(e){
	
	e.preventDefault();
	showLoader();
	actualizainfoCot();
	
	$.post( redirigeURL, {
		infoCot : JSON.stringify( infCotiJson ),
		paso : enlace.PASO2
	} ).done( function(data) {
		var response = JSON.parse( data );
		if (response.code == 0) {
			//window.location.href = response.msg;
			$("#paso3-form-back #infoCotizacion").val(response.msg);
			$("#paso3-form-back").submit();
		} else {
			showMessageError( '.navbar', response.msg, 0 );
			hideLoader();
		}
	} );
});

$("#paso3_next").click(function(e) {
	
	e.preventDefault();
	showLoader();
	actualizainfoCot();
	
	$.post( redirigeURL, {
		infoCot : JSON.stringify( infCotiJson ),
		paso : enlace.PASO4
	} ).done( function(data) {
		var response = JSON.parse( data );
		if (response.code == 0) {
			//window.location.href = response.msg;
			$("#paso3-form #infoCotizacion").val(response.msg);
			$("#paso3-form").submit();
		} else {
			showMessageError( '.navbar', response.msg, 0 );
			hideLoader();
		}
	} );
	
});

function goToPage(page){
	showLoader();
	actualizainfoCot();
	$.post( redirigeURL, {
		infoCot : JSON.stringify( infCotiJson ),
		paso : page
		/*paso : seleccionaVentana()*/
		/*paso : enlace.PASO2*/
	} ).done( function(data) {
		var response = JSON.parse( data );
		if (response.code == 0) {
			window.location.href = response.msg;
		} else {
			showMessageError( '.navbar', response.msg, 0 );
			hideLoader();
		}
	} );
}

function seleccionaVentana(){
	return enlace.FAMILIAR;
}

function actualizainfoCot(){
	switch (infCotiJson.modo) {
		case modo.NUEVA:
			infCotiJson.modo = modo.EDICION;
			break;
		case modo.COPIA:
			infCotiJson.modo = modo.EDICION;
			break;
		case modo.ALTA_ENDOSO:
			infCotiJson.modo = modo.EDITAR_ALTA_ENDOSO;
			break;
		case modo.BAJA_ENDOSO:
			infCotiJson.modo = modo.EDITAR_BAJA_ENDOSO;
			break;
		default:
			break;
	}
}


$('#polizaBtnEnviar').click(function(e) {
    showLoader();
    var emailsList = $('#listaCorreos li');
    var emailsTot = "";
    $.each(emailsList, function(i, emlis) {
        if (i > 0) {
            emailsTot += ",";
        }
        emailsTot += $(emlis).attr('email');
    });
    recuperaDocumentosEmision(emailsTot);
});

function recuperaDocumentosEmision(emails) {
    $.post($('#txtJSGetDocsEmision').val(), {
        infoDocs: jsonDocumentosEmision(),
        listaEmails: emails,
        cliente: $('#txtModalPolizaAsegurado').text(),
        poliza: $('#txtModalPolizaNumeroPoliza').text(),
        totUbica: $('#txtModalPolizaTotalUbicaciones').text(),
        moneda: $('#txtModalPolizaMoneda').text(),
        certificado: $('#txtModalPolizaCertificado').text(),
        vigencia: $('#txtModalPolizaVigenciaAl').text() + ' al ' + $('#txtModalPolizaVigenciaAl').text(),
        formaPago: $('#txtModalPolizaFormaPago').text(),
        primaNeta: '$' + $('#txtModalPolizaPrimaNeta').text(),
        recargo: '$' + $('#txtModalPolizaRecargoPago').text(),
        gasto: '$' + $('#txtModalPolizaGastosExpedicion').text(),
        iva: '$' + $('#txtModalPolizaIva').text(),
        prima: '$' + $('#txtModalPolizaTotal').text(),
        folio: infCotiJson.folio,
        agente: $('#txtModalPolizaAgente').val()
    }).done(function(data) {
        sessionExtend();
        var respuestaJson = JSON.parse(data);
        if (respuestaJson.code >= 0) {
            if (emails == null) {
                $.each(respuestaJson.archivos, function(i, archivo) {
                	/*
                    fileAux = 'data:application/octet-stream;base64,' + archivo.documento
                    var dlnk = document.getElementById(archivo.nombre + archivo.extension);
                    dlnk.href = fileAux;
                    dlnk.download = archivo.nombre + '.' + archivo.extension;
                    dlnk.click();
                    */
                	if(detectIEEdge()){
    					fileAux = 'data:application/octet-stream;base64,'+archivo.documento
    					var dlnk = document.getElementById('dwnldLnk');
    					dlnk.href = fileAux;
    					dlnk.download = archivo.nombre+'.'+archivo.extension;
    					location.href=document.getElementById("dwnldLnk").href;
    					/*dlnk.click();*/
    				}else{
    					/*
    					 * downloadDocument('archivo base 64' , 'nombre.extension' );
    					 */
    					downloadDocument(archivo.documento, archivo.nombre+'.'+archivo.extension);
    				}
                });
            } else {
                showMessageSuccess('#modalGenerarPoliza', "Correo(s) enviado(s)", 0);
            }
        } else {
            showMessageError('#modalGenerarPoliza', respuestaJson.msg, 0);
        }
    }).fail(function() {
        showMessageError('#modalGenerarPoliza', "Error al consultar la informacion", 0);
    }).always(function() {
        hideLoader();
    });
}

$('#btnDescargarArchivos').click(function(e) {
    showLoader();
    recuperaDocumentosEmision(null);
});

$('#txtCederComision').on('keyup', function() {
    $(event.target).val(function(index, value) {
        var aux = value.replace(/\D/g, "")
        if (parseInt(aux) > 100) {
            showMessageError('.navbar', 'La comisión no pude superar el 100% ', 0);
            return '100';
        }
        return aux;
    });
});




$("#renovacion_back").click(function(){
	showLoader();
	var url = new URL(window.location.href);
	window.location.href = url.origin + url.pathname.replace("paso3", "renovacion-automatica");
	
})

$('#btnEnvCotiSusAgente').click(function(e) {
	showLoader();
	e.preventDefault();
				
	var url = new URL(window.location.href);
	var auxUrl = url.pathname;
	
	$.post( sendMailSuscriptorAgenteURL , {
	    cotizacion: infCotiJson.cotizacion,
	    version: infCotiJson.version,
	    folio: infCotiJson.folio,
	    url: url.origin + auxUrl,
	    tipoCotizacion: infCotiJson.tipoCotizacion.toString(),
	    email: $('#txtEmailAgente').val()
	}).done(function() {
		goToHome();
	});

});

$('#btnContinuarJK').click(function() {
    showLoader();
	
	$.post(continuarJKURL, 
		{
			cotizacion: infCotiJson.cotizacion,
			version: infCotiJson.version,
			pantalla: infCotiJson.pantalla,
		})
	.done(function(data){
		var response = JSON.parse(data);
		showMessageSuccess('.navbar', response.msg, 0);
		hideLoader();
	});
	
    /*
    $.post( sendMailSuscriptorAgenteURL , {
        cotizacion: infCotiJson.cotizacion,
        version: infCotiJson.version,
        folio: infCotiJson.folio,
        url: url.origin + auxUrl,
        tipoCotizacion: infCotiJson.tipoCotizacion.toString(),
        email: $('#txtEmailAgente').val()
    }).done(function() {
    	goToHome();
    });

	*/
});


/***************************************************Funciones Botones*************************************************/
/*********************************************************************************************************************/
/**************************************************Funciones Genericas************************************************/

function validaBotonEmision(estado) {
	if(perfilSuscriptor == 1) {
		$.post(getPermisoEmisionURL, {})
			.done(function(dataE){
				var response = JSON.parse(dataE);
				
				if(response.code == 0) {
					
					$.post(validaAgenteURL, {
	            		cotizacion: infCotiJson.cotizacion,
	        	    	codigoAgente: ''
	        	    }).done(function(data) {
	        	    	
	        	    	var response = JSON.parse(data);
	        	    	
	        	    	if(response.code != 0) {
	        	    		if(response.code == 3) {
	        	    			$("#modalBloqueoAgente").modal('show');
	        	    			$("#paso3_next").prop("disabled", true);
	        	    		}
	        	    		else {
	        	    			showMessageError('.navbar', response.msg, 0);
	        	    		}
	        	    	}
	        	    	else {
	        	    		
					$("#paso3_next").prop("disabled", false);
					$("#paso3_next").removeClass('d-none');
				}
	        	    });
				}
				else {
					$("#paso3_next").attr("disabled", true);
				}
			});
	}
	else {
	    if (estado == 340 || estado == 350 || estado == 351) {
	    	
	    	$.post(validaAgenteURL, {
        		cotizacion: infCotiJson.cotizacion,
    	    	codigoAgente: ''
    	    }).done(function(data) {
    	    	
    	    	var response = JSON.parse(data);
    	    	
    	    	if(response.code != 0) {
    	    		if(response.code == 3) {
    	    			$("#modalBloqueoAgente").modal('show');
    	    			$("#paso3_next").prop("disabled", true);
    	    		}
    	    		else {
    	    			showMessageError('.navbar', response.msg, 0);
    	    		}
    	    	}
    	    	else {
    	    		
	    	$("#paso3_next").prop("disabled", false);
			$("#paso3_next").removeClass('d-none');
    	    	}
    	    });
	    } else {
	        $("#paso3_next").attr("disabled", true);
	    }
	}
}

function resetSession() {
    try {
        Liferay.Session.extend();
    } catch (err) {

    }
}

function consumeEmisionData() {
    showLoader();
    $.post($('#txtJSGetEmisionData').val(), {
        cotizacion: infCotiJson.cotizacion,
        version: infCotiJson.version
    }).done(function(data) {
        sessionExtend();
        var response = jQuery.parseJSON(data);
        if (response.code == 0) {
        	/*
            varAuxiliares.tipoPersona = isFisica_Moral(response.datosCliente.tipoPer);
            preCargaDatos(response);
            */
            goToPage(enlace.PASO4);
            /*hideLoader();*/
        } else {
            showMessageError('.navbar', 'Error al consultar la información', 0);
            hideLoader();
        }
        /*
        if (valIsNullOrEmpty($('#txtBtnEmiteFactu').val())) {
            validaPrimaMax492(0);
        }
        */
    }).fail(function(e) {
        showMessageError('.navbar', 'Error al consultar la información (data)', 0);
        hideLoader();
    });
}

function llenaInfoModalPoliza(json) {
    $('.listaCorreos li').remove();
    $('#txtModalPolizaNumeroPoliza').text(validaKeyJson(json, 'numeroPoliza'));
    $('#txtModalPolizaCertificado').text(validaKeyJson(json, 'certificado'));
    $('#txtModalPolizaAsegurado').text(validaKeyJson(json, 'asegurado'));
    $('#txtModalPolizaAgente').val(validaKeyJson(json, 'agente'));

    $('#txtModalPolizaVigenciaDe').text(stringToDate(validaKeyJson(json, 'vigencia.inicio')));
    $('#txtModalPolizaVigenciaAl').text(stringToDate(validaKeyJson(json, 'vigencia.fin')));
    $('#divDescargarArchivos').html();

    $('#txtModalPolizaTotalUbicaciones').text(validaKeyJson(json, 'totalUbicaciones'));
    $('#txtModalPolizaMoneda').text(validaKeyJson(json, 'moneda'));
    $('#txtModalPolizaMoneda').text(validaKeyJson(json, 'moneda'));
    $('#txtModalPolizaFormaPago').text(validaKeyJson(json, 'formaPago'));
    $('#txtModalPolizaPrimaNeta').text(validaKeyJson(json, 'primaNeta'));
    $('#txtModalPolizaRecargoPago').text(validaKeyJson(json, 'recargo'));
    $('#txtModalPolizaGastosExpedicion').text(validaKeyJson(json, 'gastos'));
    $('#txtModalPolizaIva').text(validaKeyJson(json, 'iva'));
    $('#txtModalPolizaTotal').text(validaKeyJson(json, 'total'));
    $('#tablaArchivosPoliza tbody').empty();
    if (!valIsNullOrEmpty($('#txtEmailUser').val())) {
        $('.modal .listaCorreos')
            .append(
                $('<li email="' +
                	Base64.decode( $('#txtEmailUser').val() ) +
                    '" ><button type="button" class="close" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
                    Base64.decode( $('#txtEmailUser').val() ) + '</li>'));
    }

    if (valIsNullOrEmpty(validaKeyJson(json, 'archivos'))) {
        $('.selectCheckImput').prop('checked', false);
        $('.selectCheckImput').prop("disabled", true);
        $('#btnDescargarArchivos').prop("disabled", true);
        $('#polizaBtnEnviar').prop("disabled", true);
    } else {
        $('.selectCheckImput').prop('checked', true);
        $('.selectCheckImput').prop("disabled", false);
        $('#btnDescargarArchivos').prop("disabled", false);
        validaBtnEnviar();
        $.each(json.archivos, function(i, a) {
            var chekbox = '<div class="form-check"> ' + '<input class="form-check-inpu chekArchivos" name="' + a.nombre +
                "-" + a.extension + '" idCarpeta="' + a.idCarpeta + '" idDocumento="' + a.idDocumento +
                '" idCatalogoDetalle="' + a.idCatalogoDetalle + '" type="checkbox" id="' + a.nombre + "-" +
                a.extension + '" checked>' + '<label for="' + a.nombre + "-" + a.extension + '"></label>' +
                '</div>';

            $('#tablaArchivosPoliza tbody').append(
                $('<tr> <td> ' + chekbox + ' </td> <td>  ' + a.nombre + "." + a.extension + ' </td> <td >  ' +
                    a.tipo + ' </td> </tr>'));

            $('#divDescargarArchivos').append($('<a id="' + a.nombre + a.extension + '" />'));

        });
    }
}

function validaKeyJson(json, cadena) {
    var infoJson = '';
    var res = cadena.split(".");
    var ant = null;
    json['name']
    $.each(res, function(key, val) {
        if (key == 0) {
            infoJson = (val in json) ? eval('json.' + val) : "";
            ant = eval('json.' + val);
        } else {
            if (valIsNullOrEmpty(ant)) {
                infoJson = "";
            } else {
                infoJson = (val in ant) ? eval('ant.' + val) : "";
                ant = eval('ant.' + val);
            }
        }
    });
    return infoJson;
}

function validaBtnEnviar() {
    if ($('.listaCorreos li').length > 0) {
        $('#polizaBtnEnviar').prop("disabled", false);
        $('.msjActivarBtnEnviar').prop('hidden', true);
    } else {
        $('#polizaBtnEnviar').prop("disabled", true);
        $('.msjActivarBtnEnviar').prop('hidden', false);
    }
}

$('#txtPrimaObj').on('keyup', function() {
    $(event.target).val(function(index, value) {
        var aux = value.replace(/[$,]/g, '');
        aux = aux.replace(/\D/g, "")
            .replace(/([0-9])([0-9]{2})$/, '$1.$2')
            .replace(/\B(?=(\d{3})+(?!\d)\.?)/g, ",");
        return '$' + aux;
    });
});

function setCoinFormat(num) {
	num = "" + num;
	if( num ==""){
		return num;
	}
	
	arraySplit = num.split(".");
	izq = arraySplit[0];
	der = "00";
	if ( num.includes(".") ) {
		der = arraySplit[1];
	}
	izq = izq.replace(/ /g, "");
	izq = izq.replace(/\$/g, "");
	izq = izq.replace(/,/g, "");

	var izqAux = "";
	var j = 0;
	for ( i = izq.length - 1; i >= 0; i-- ) {
		if ( j != 0 && j % 3 == 0 ) {
			izqAux += ",";
		}
		j++;
		izqAux += izq[i];
	}
	izq = "";
	for ( i = izqAux.length - 1; i >= 0; i-- ) {
		izq += izqAux[i];
	}
	der = der.substring(0, 2);
	if ( der.length < 2 ) {
		der += "0";
	}
	return "$" + izq + "." + der;
}

$("#modalPolizaEnviarCorreo").keyup(function(e) {
    if (valIsNullOrEmpty($(this).val())) {
        $('#btnAgregaCorreoPoliza').prop("disabled", true);
    } else {
        $('#btnAgregaCorreoPoliza').prop("disabled", false);
        eliminaErrorEmailEmision();
    }
    var code = (e.keyCode ? e.keyCode : e.which);
    if (code == 13) {
        $('#btnAgregaCorreoPoliza').trigger('click');
    }
});

function eliminaErrorEmailEmision() {
    $("#modalPolizaEnviarCorreo").removeClass('invalid');
    $("#modalPolizaEnviarCorreo").siblings('.alert-danger').remove();
}
$('#btnAgregaCorreoPoliza').click(function(e) {
    var correo = $('#modalPolizaEnviarCorreo').val();
    var error = chekEmail($('#modalPolizaEnviarCorreo'));
    if ((!error) && (!valIsNullOrEmpty(correo))) {
        $('.modal .listaCorreos').append(
            $('<li email="' + correo +
                '"><button type="button" class="close" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
                correo + '</li>'));
        $('#modalPolizaEnviarCorreo').val('');
        $('#listaCorreos').scrollTo('li:last');
        $('#btnAgregaCorreoPoliza').prop("disabled", true);
    }
    validaBtnEnviar();
    if (!almenosUnArchivoSeleccionado()) {
        $('#polizaBtnEnviar').prop("disabled", true);
    }
});

function chekEmail(campos) {
    var errores = false;
    $.each(campos, function(index, value) {
        if (!valIsNullOrEmpty($(value).val())) {
            if (!validateEmail($(value).val())) {
                errores = true;
                $(value).addClass('invalid');
                $(value).parent().append(
                    "<div class=\"alert alert-danger\" role=\"alert\"> <span class=\"glyphicon glyphicon-ban-circle\"></span>" +
                    " " + $('#txtFormatoEmail').val() + "</div>");
            }
        }
    });
    return errores;
}

function validateEmail(email) {
    var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    return re.test(String(email).toLowerCase());
}

$('.modal .listaCorreos').on('click', '.close', function(e) {
    $(this).parent().remove();
    validaBtnEnviar();
});

function jsonDocumentosEmision() {
    var cheks = $('#modalGenerarPoliza .bodyArchivos input[type=checkbox]');
    var listaDocumentos = '';
    if ($('.selectCheckImput').is(':checked')) {
        $.each(cheks, function(i, chek) {
            var ids = $(chek).attr('id');
            if (i > 0) {
                listaDocumentos += ",";
            }
            listaDocumentos += '{"idCarpeta" : ' + $(chek).attr('idCarpeta') + ', "idDocumento" : ' +
                $(chek).attr('idDocumento') + ', "idCatalogoDetalle" : ' +
                $(chek).attr('idCatalogoDetalle') + ', "documento" : "", "nombre" : "", "extension" : "" }';
        });
    } else {
        $.each(cheks, function(i, chek) {
            if ($(chek).is(':checked')) {
                var ids = $(chek).attr('id');
                if (!valIsNullOrEmpty(listaDocumentos)) {
                    listaDocumentos += ",";
                }
                listaDocumentos += '{"idCarpeta" : ' + $(chek).attr('idCarpeta') + ', "idDocumento" : ' +
                    $(chek).attr('idDocumento') + ', "idCatalogoDetalle" : ' +
                    $(chek).attr('idCatalogoDetalle') +
                    ', "documento" : "", "nombre" : "", "extension" : "" }';
            }
        });
    }
    return listaDocumentos;
}

function almenosUnArchivoSeleccionado() {
    var seleccionado = false;
    $.each($('.modal .bodyArchivos .chekArchivos'), function(i, chek) {
        if ($(chek).is(':checked')) {
            seleccionado = true;
            return false;
        }
    });
    return seleccionado;
}

function compruebaBqOri(){	
	$.each($('.bqOri'), function(key, val) {
		if($(val).val() == ''){
			$(val).removeClass('bqOri');
		}
	});
}

function detectIEEdge() {
    var ua = window.navigator.userAgent;

    var msie = ua.indexOf('MSIE ');
    if (msie > 0) {
        // IE 10 or older => return version number
        console.log(parseInt(ua.substring(msie + 5, ua.indexOf('.', msie)), 10));
        return true;
    }

    var trident = ua.indexOf('Trident/');
    if (trident > 0) {
        // IE 11 => return version number
        var rv = ua.indexOf('rv:');
        console.log(parseInt(ua.substring(rv + 3, ua.indexOf('.', rv)), 10));
        return true;
    }

    var edge = ua.indexOf('Edge/');
    if (edge > 0) {
        // Edge => return version number
        console.log(parseInt(ua.substring(edge + 5, ua.indexOf('.', edge)), 10));
        return true;
    }

    // other browser
    return false;
}

function downloadDocument(strBase64, filename) {
    var url = "data:application/octet-stream;base64," + strBase64;
    var documento = null;
    /*.then(res => res.blob())*/
    fetch(url)
        .then(function(res) { return res.blob() })
        .then(function(blob) {
            downloadBlob(blob, filename);
        });
}

function downloadBlob(blob, filename) {
    if (window.navigator.msSaveOrOpenBlob) {
        window.navigator.msSaveBlob(blob, filename);
    } else {
        var elem = window.document.createElement('a');
        elem.href = window.URL.createObjectURL(blob);
        elem.download = filename;
        document.body.appendChild(elem);
        elem.click();
        document.body.removeChild(elem);
    }
}

function validaModo() {
	switch (infCotiJson.modo) {
		case modo.CONSULTA:
			$('#divCederComision').addClass('d-none');
			$('#divPrimaObj').addClass('d-none');
			$('#paso3_slip').attr('disabled', true);
			$('#paso3_next').removeClass('d-none');
			
			$.post(validaAgenteURL, {
        		cotizacion: infCotiJson.cotizacion,
    	    	codigoAgente: ''
    	    }).done(function(data) {
    	    	
    	    	var response = JSON.parse(data);
    	    	
    	    	if(response.code != 0) {
    	    		if(response.code == 3) {
    	    			$("#modalBloqueoAgente").modal('show');
    	    			$("#paso3_next").prop("disabled", true);
    	    		}
    	    		else {
    	    			showMessageError('.navbar', response.msg, 0);
    	    		}
    	    	}
    	    	else {
    	    		
    	    		$("#paso3_next").prop("disabled", false);
    				$("#paso3_next").removeClass('d-none');
    	    	}
    	    });
			
			break;
		case modo.BAJA_ENDOSO:
			
			$('#paso3_emision').addClass('d-none');
			$('#btnEmitEndoso').removeClass('d-none');
			
			generaTblsEndBaja();
			
			$('#paso3_back').addClass('d-none');
			$('#paso1_back').removeClass('d-none');
			agregaTipoMonedaPT();
			break;
		case modo.EDITAR_BAJA_ENDOSO:
			agregaTipoMonedaPT();
			$('#paso3_emision').addClass('d-none');
			$('#btnEmitEndoso').removeClass('d-none');
			
			generaTblsEndBaja();
			$('#paso3_back').addClass('d-none');
			$('#paso1_back').removeClass('d-none');
			break;
		case modo.ALTA_ENDOSO:
			agregaTipoMonedaPT();
			break;
		case modo.EDITAR_ALTA_ENDOSO:
			agregaTipoMonedaPT();
			break;
		case modo.RENOVACION_AUTOMATICA :
			generaPantallaRenovacion();
			break;
		case modo.CONSULTAR_RENOVACION_AUTOMATICA :
			generaPantallaRenovacion();
			$('#paso3_slip').attr('disabled', true);
			break;
		case modo.FACTURA_492 :
			$("#tabPaso3_2 a").addClass('d-none');
			$("#btnComisionesAgente").addClass('d-none');
			break;
		case modo.EDICION_JAPONES:
			
			$.post(validaAgenteURL, {
        		cotizacion: infCotiJson.cotizacion,
    	    	codigoAgente: ''
    	    }).done(function(data) {
    	    	
    	    	var response = JSON.parse(data);
    	    	
    	    	if(response.code != 0) {
    	    		if(response.code == 3) {
    	    			$("#modalBloqueoAgente").modal('show');
    	    			$("#paso3_next").prop("disabled", true);
    	    		}
    	    		else {
    	    			showMessageError('.navbar', response.msg, 0);
    	    		}
    	    	}
    	    	else {
    	    		
    	    		$("#paso3_next").prop("disabled", false);
    				$("#paso3_next").removeClass('d-none');
    	    	}
    	    });
			break;
		case modo.COPIA:
			
			$.post(validaAgenteURL, {
        		cotizacion: infCotiJson.cotizacion,
    	    	codigoAgente: ''
    	    }).done(function(data) {
    	    	
    	    	var response = JSON.parse(data);
    	    	
    	    	if(response.code != 0) {
    	    		if(response.code == 3) {
    	    			$("#modalBloqueoAgente").modal('show');
    	    			$("#paso3_next").prop("disabled", true);
    	    		}
    	    		else {
    	    			showMessageError('.navbar', response.msg, 0);
    	    		}
    	    	}
    	    	else {
    	    		
    	    		$("#paso3_next").prop("disabled", false);
    				$("#paso3_next").removeClass('d-none');
    	    	}
    	    });
			break;
		default:

			break;
	}
}

function generaPantallaRenovacion(){
	agregaTipoMonedaPT();
	$("#paso3_back").addClass("d-none");
	$("#renovacion_back").removeClass("d-none");
	$("#titPoliza").text("Renovación de Póliza " + infCotiJson.poliza);
}

function generaTblsEndBaja(){
	$('#divTbl').addClass('d-none');
	$('#divTblEndBj').removeClass('d-none');
	$("#titulosEndBj").append(tablaBajasEndoso);
	$("#datosEndBj").append(tablaBajasEndoso);
	$("#totalEndBj").append(tablaBajasEndoso);
	$("#titulosEndBj .tb2").addClass("d-none");
	$("#titulosEndBj .tb3").addClass("d-none");
	$("#datosEndBj .tb1").addClass("d-none");
	$("#datosEndBj .tb3").addClass("d-none");
	$("#totalEndBj .tb1").addClass("d-none");
	$("#totalEndBj .tb2").addClass("d-none");

}

function agregaTipoMonedaPT(){
	var p1json = JSON.parse(infP1);	
	$("#valPrimTot").addClass("pt_mon");
	if(p1json.monedaSeleccionada == "1"){
		$("#valPrimTot #tabPaso3_3").text($("#valPrimTot #tabPaso3_3").text() + 'MXN');
	}else{
		$("#valPrimTot #tabPaso3_3").text($("#valPrimTot #tabPaso3_3").text() + 'USD')
	}

}

function goToHome(){
	showLoader();
	var urlHome = window.location.origin + '/group/portal-agentes' + seleccionaVentana();
	var url = new URL(window.location.href);
	var aux = url.pathname.replace("/paso3", seleccionaVentana());
	window.location.href = url.origin + aux;
}


function noSelect(campo) {
    var errores = false;
    if ($(campo).val() == "-1") {
        errores = true;
        $(campo).siblings("input").addClass('invalid');
        $(campo).parent().append(
            "<div class=\"alert alert-danger\"> <span class=\"glyphicon glyphicon-ban-circle\"></span> " + " " +
            msj.es.campoRequerido + "</div>");
    }
    
    return errores;
}

$("#btnComisionesAgente").click(function(){
	$.post(comisionesAgenteURL,{
		cotizacion: infCotiJson.cotizacion,
		version: infCotiJson.version,
		pantalla: infCotiJson.pantalla
	})
		.done(function(data){
			responseComision = JSON.parse(data);
			$("#tableComisionesBody").empty();
			$.each(responseComision.lista, function(index, value){
				/*$("#tableComisionesBody").append('<tr><td>' + value.ramo + '</td><td>' + value.descripcion + "</td><td class='text-right comision' ramo='"+value.ramo+"' valorMin='"+ value.min_valor +"' valorMax='" + value.max_valor + "' contenteditable='true'> <input type='text' class='comision2'> " + value.comision + '</td></tr>')*/
				$("#tableComisionesBody").append('<tr><td>' + value.ramo + '</td><td>' + value.descripcion + '</td><td class="text-right comision" ramo="'+value.ramo+'" valorMin="'+ value.min_valor +'" valorMax="' + value.max_valor + '"> <input type="text" class="auxPorcen" value="' + value.comision + '"> </td></tr>')
			});
			$("#modalComisionesAgente").modal('show');
		});
});

$("#btnGuardarComisionesAgente").click(function(){
	generaComosionesAgente();
	guardaComosionesAgente();
});

function generaComosionesAgente(){
	flagValMax = true;
	comsisionesAgArr = [];
	
	$.each($("#modalComisionesAgente .comision"), function(index, value){
			var comisionAux = new Object();
			
			comisionAux.codigo_ramo = $(this).attr('ramo');
			comisionAux.valor =  parseFloat($(this).find('input').val());
			
			if ( comisionAux.valor  > parseFloat($(this).attr('valormax')) ) {
				flagValMax = false;
				$(this).addClass('colorRed')
			}
			
			comsisionesAgArr.push(comisionAux);
	});
	console.log( JSON.stringify(comsisionesAgArr) );
	console.log('flag: ' + flagValMax);
}

function guardaComosionesAgente(){
	if( flagValMax ){
		$.post(guardaComisionesAgenteURL,{
			cotizacion: infCotiJson.cotizacion,
			version: infCotiJson.version,
			pantalla: infCotiJson.pantalla,
			comisiones: JSON.stringify(comsisionesAgArr)
		}).done(function(data){
			console.log(data);
			var jsonResponse = JSON.parse(data);
			if(jsonResponse.code == 0 ){
				$("#modalComisionesAgente").modal('hide');
				showMessageSuccess('.navbar', jsonResponse.msg, 0);
			}
		});		
	}else{
		showMessageError( '#modalComisionesAgente .modal-header', "La comisión ingresada no debe ser mayor a la comisión máxima del ramo", 0 );
	}
}

$( "#tableComisionesBody" ).on( "click", ".colorRed input", function() {
  $(this).parent().removeClass('colorRed');
});

$( "#tableComisionesBody" ).on("keyup", ".auxPorcen",function() {
	validaCampoPorcentaje();
});

function validaCampoPorcentaje(){
	$(event.target).val(function(index, value) {
		 var aux = value.replace(/[$,]/g, '');
		 aux = aux.replace(/\D/g, "")
		 .replace(/([0-9])([0-9]{2})$/, '$1.$2')
		 .replace(/\B(?=(\d{3})+(?!\d)\.?)/g, ",");
	        
		 aux = aux.replace(/\,/g, '');
		 
		 if (parseInt(aux) > 100) {
			 /*showMessageError('.navbar', 'El porcentaje no puede superar el 100% ', 0);*/
			 return '100.00';
		 }
		 var res = aux.split(".");
		 if( res[0].length > 3){
			 /*showMessageError('.navbar', 'El porcentaje mínimo es 00.000001% ', 0);*/
			 return '0.01'
		 }
	        
		 return aux;
	 });
}

$("#btnCalculoPrimaSuscriptor").click(function(){
	$.post(primaObjetivoSuscriptorURL,
		{
			cotizacion: infCotiJson.cotizacion,
			version: infCotiJson.version,
			pantalla: infCotiJson.pantalla
		})
		.done(function(data){
			responseComision = JSON.parse(data);
			$("#tablePrimasBody").empty();
			$.each(responseComision.lista_primas, function(index, value){
				$("#tablePrimasBody").append(
					'<tr id="' + value.ramo + '" class="rowRamo"><td>' + value.descripcion + '</td><td>' + setCoinFormat('' + value.suma_asegurada) + '</td>' + 
					'<td>' + setCoinFormat('' + value.prima) + '</td><td> ' + value.cuota_original + '</td><td id="cuotaFinal">' + value.cuota_final + '</td>' + 
					'<td class="number"><input type="text" id="primaNueva" class="moneda primaNueva" value="' + setCoinFormat('' + value.prima_objetivo) + '">' + 
					'</td><td class="number"><input type="text" id="descuento" class="descuento" value="' + value.descuento + '"></td></tr>');
					
				console.log(value);
			});
			$("#modalPrimaObjetivo").modal('show');
		});
});

$("#tablePrimasBody").on('blur', '.descuento, .primaNueva', function() {
	
	var auxPrimas = [];
	
	var objAux = new Object;
	objAux.p_codigo_ramo = $(this).closest('.rowRamo').attr('id');
	objAux.p_prima_objetiva = parseFloat(valIsNullOrEmpty($(this).closest('.rowRamo').find('#primaNueva').val()) ?
            0 : $(this).closest('.rowRamo').find('#primaNueva').val().replace(/[$,]/g, ''));
	objAux.p_descuento = $(this).closest('.rowRamo').find('#descuento').val();
	
	auxPrimas.push(objAux);
	
	var auxCuotaFinal = $(this).closest('.rowRamo').find('#cuotaFinal');
	
	$.post(getCuotaFinalURL
		,{
			cotizacion: infCotiJson.cotizacion,
			version: infCotiJson.version,
			pantalla: infCotiJson.pantalla,
			lista: JSON.stringify(auxPrimas)
		}
	).done(function(data) {
		var response = JSON.parse(data);
		
		if(response.code == 0) {
			auxCuotaFinal.text(response.p_cuota_final);
		}
		else{
			showMessageError('#modalPrimaObjetivo .modal-header', response.msg, 0);
		}
		
		console.log(response);
	});
});

$("#btnGuardarPrimaObjetivo").click(function() {
	
	var auxPrimas = generaPrimas();
	var gastos = parseFloat(valIsNullOrEmpty($('#gastos').val()) ?
            0 : $('#gastos').val().replace(/[$,]/g, ''));
	var recargoPago = parseFloat(valIsNullOrEmpty($('#recargoPago').val()) ?
        0 : $('#recargoPago').val().replace(/[$,]/g, ''));

	$.post(
		validaPrimaURL,
		{
			cotizacion: infCotiJson.cotizacion,
			version: infCotiJson.version,
			pantalla: infCotiJson.pantalla,
			lista: JSON.stringify(auxPrimas)
		}
	).done(function(data){
		var response = JSON.parse(data);
		
		console.log(response);
		
		if(response.code == 0) {
				$.post(
					guardaPrimaURL,
					{
						cotizacion: infCotiJson.cotizacion,
						version: infCotiJson.version,
						pantalla: infCotiJson.pantalla,
						lista: JSON.stringify(auxPrimas),
						gastos: gastos,
						recargoPago: recargoPago
					}
				).done(function(dataG){
					
					var responseGuardar = JSON.parse(dataG);
					
					if(responseGuardar.code == 0) {
						$("#modalPrimaObjetivo").modal('hide');
						
						$('#txtEmailAgente').val(responseGuardar.email);
	                    var band = null;
						var bandInt = null;
	                    $('#tabPaso3').html("");
	                    $('#tabPaso3').html("");
                    	$.each(responseGuardar.datosCaratula, function(k, valCaratula) {
	                        if (!(valCaratula.contenedor == band)) {
	                            band = valCaratula.contenedor;
	
								if(perfilSuscriptor == 1 || perfilJapones == 1) {
									$('#tabPaso3').append("<tr><th>" + band + "</td><td></td><td></td><td></th></tr>");
								}
								else{
									$('#tabPaso3').append("<tr><th>" + band + "</td><td></td><td></th></tr>");
								}
	                            
	                        }
	
							if(perfilSuscriptor == 1 || perfilJapones == 1){
								$('#tabPaso3').append("<tr><td>" + valCaratula.titulo + "</td><td class=\"number\">" + valCaratula.sa + "</td><td class=\"number\">" + valCaratula.prima + "</td><td>" + valCaratula.deducible + "</td></tr>");	
							}
							else{
								$('#tabPaso3').append("<tr><td>" + valCaratula.titulo + "</td><td class=\"number\">" + valCaratula.sa + "</td><td>" + valCaratula.deducible + "</td></tr>");
							}
	                        
	                    });
	
	                    $('#tabPaso3_2').html("<tr><td>Prima Neta:</td><td id='primaNeta' class=\"number\">" + setCoinFormat('' + responseGuardar.primaNeta) + "</td></tr>");                    
	
						if(perfilSuscriptor == 1 || perfilJapones == 1) {
							$('#tabPaso3_2').append("<tr><td>Recargo por Pago Fraccionado:</td><td class=\"number\"><input id=\"recargoPago\" class=\"moneda campoEditable\" value=\"" + setCoinFormat('' + responseGuardar.recargo) + "\" disabled=\"true\" /></td><td><a onclick=\"editarCamposPrima('recargoPago')\" style=\"color: #0275d8; text-decoration: underline;\">Editar</a></td></tr>");
	                		$('#tabPaso3_2').append("<tr><td>Gastos de Expedición:</td><td class=\"number\"><input id=\"gastos\" class=\"moneda campoEditable\" value=\"" + setCoinFormat('' + responseGuardar.gastos) + "\" disabled=\"true\" /></td><td><a onclick=\"editarCamposPrima('gastos')\" style=\"color: #0275d8; text-decoration: underline;\">Editar</a></td></tr>");	
						}
						else {
							$('#tabPaso3_2').append("<tr><td>Recargo por Pago Fraccionado:</td><td class=\"number\"><input id=\"recargoPago\" class=\"moneda campoEditable\" value=\"" + setCoinFormat('' + responseGuardar.recargo) + "\" disabled=\"true\" /></td></tr>");
	                    	$('#tabPaso3_2').append("<tr><td>Gastos de Expedición:</td><td class=\"number\"><input id=\"gastos\" class=\"moneda campoEditable\" value=\"" + setCoinFormat('' + responseGuardar.gastos) + "\" disabled=\"true\" /></td></tr>");
						}
	
	                    $('#tabPaso3_2').append("<tr><td>I.V.A.:</td><td class=\"number\">" + setCoinFormat('' + responseGuardar.iva) + "</td></tr>");
	                    $('#tabPaso3_3').html(setCoinFormat('' + responseGuardar.total));
	                    
						var tipoMonSelect = parseInt($('#dc_moneda').val());
	                    if (tipoMonSelect == 1) {
	                        $('#titPrimaObj').text("Prima Objetivo: (Pesos)");
	                    } else {
	                        $('#titPrimaObj').text("Prima Objetivo: (Dolares)");
	                    }
	
	                    hideLoader();
	                    showMessageSuccess('.navbar', 'Información actualizada correctamente', 0);
	
						$("#paso3_slip").attr('disabled', false);
						$("#paso3_next").attr('disabled', true);
						$("#paso3_next").addClass('d-none');
						banderaEditar = false;
					}
					else { 
						showMessageError('#modalPrimaObjetivo .modal-header', responseGuardar.msg, 0);
					}
				});
		}
		else {
			showMessageError( '#modalPrimaObjetivo .modal-header', response.msg, 0);
			console.log(response.lista_validaciones);
			$.each(response.lista_validaciones, function(key, value) {
				$("#" + value + " input").css('color', 'red');
			});
		}
	});
});

$("#tablePrimasBody").on('click', 'input', function(){
	$(this).removeAttr('style');
});

function generaPrimas() {
	
	var arrayPrimas = [];
	
	$.each($(".rowRamo"), function(key, value){
		
		var auxId = $(this).attr('id');
		
		var auxObj = new Object();
		
		auxObj.p_codigo_ramo = auxId;
		auxObj.p_prima_objetiva = parseFloat(valIsNullOrEmpty($("#" + auxId + " #primaNueva").val()) ?
            0 : $("#" + auxId + " #primaNueva").val().replace(/[$,]/g, ''));
		auxObj.p_descuento = parseFloat(valIsNullOrEmpty($("#" + auxId + " #descuento").val()) ?
            0 : $("#" + auxId + " #descuento").val().replace(/[$,]/g, ''));
		
		arrayPrimas.push(auxObj);
	});
	
	return arrayPrimas;
}

$("#paso3_slip_word").click(function(e) {
    resetSession();
    showLoader();
    e.preventDefault();
    $.post($("#getSlip").val(), {
        cotizacion: infCotiJson.cotizacion,
        version: infCotiJson.version,
		pantalla: infCotiJson.pantalla,
		folio: infCotiJson.folio,
		word: 1
    }).done(function(data) {

        var respuestaJson = JSON.parse(data);
        if (respuestaJson.code == 0) {
		
            var buffer = new Uint8Array(respuestaJson.documento);
            var blob = new Blob([buffer], { type: "application/pdf" });
            $("#aPdf").attr("href", window.URL.createObjectURL(blob));
            var link = document.getElementById("aPdf");
            link.download = respuestaJson.nombre + respuestaJson.extension;
            link.click();
            validaBotonEmision(respuestaJson.estado);
            $("#btnEnvCotiSusAgente").prop("disabled", false);
            $("#btnEmitEndoso").prop("disabled", false);
            
            $("#paso3_carga_slip_word").removeClass('d-none');
		
            hideLoader();
        } else {
            /*agregaAlertError("Mensaje: " + respuestaJson.msg);*/
        	showMessageError(".navbar", "Mensaje: " + respuestaJson.msg, 1);
            hideLoader();
        }
    });
});

$("#paso3_carga_slip_word").click(function() {
	$("#modalSlipWord").modal('show');
});

function guardarSlipWord() {
	
	showLoader();
	
	var dataDoc = new FormData();
	
	var url = new URL(window.location.href);
	var auxiliarDoc = '{';

    $.each($('#archivoSlip')[0].files, function(i, file) {
        dataDoc.append('file-' + i, file);
        var nomAux = file.name.split('.');

		if(nomAux.length > 2) {
			
			var nomAux2 = "";
			
			for(i = 0; i < (nomAux.length - 1); i++) {
				nomAux2 += nomAux[i] + ".";
			}
			
			nomAux2 = nomAux2.slice(0,-1);
			
			auxiliarDoc += '\"plantillaSlip\" : {';
	        auxiliarDoc += '\"nom\" : \"' + nomAux2 + '\",';
	        auxiliarDoc += '\"ext\" : \"' + nomAux[nomAux.length - 1] + '\"}';
		}
		else {
	        auxiliarDoc += '\"plantillaSlip\" : {';
	        auxiliarDoc += '\"nom\" : \"' + nomAux[0] + '\",';
	        auxiliarDoc += '\"ext\" : \"' + nomAux[1] + '\"}';
		}
    });
    auxiliarDoc += '}';

	dataDoc.append('auxiliarDoc', auxiliarDoc);
	dataDoc.append('infoCot', JSON.stringify(infCotiJson));
	dataDoc.append('url', url.origin + url.pathname); 
    dataDoc.append('url2', url.origin);
	
	$.ajax( {
		url : cargaSlipWordURL, 
		data : dataDoc, 
		processData : false, 
		contentType : false, 
		type : 'POST',
		async: false,
		success : function(data) {
			console.log(data);
			
			hideLoader();
			
			var response = JSON.parse(data);
			
			if(response.msg == "OK") {
				$("#modalSlipWord").modal('hide');
				
				$("#btnEnvCotiSusAgente").prop("disabled", false);
            	$("#btnEnvCotiSusAgente").removeClass('d-none');
				
			}
			else{
				showMessageError('#modalSlipWord .modal-header', response.msg, 0);
			}
			
		}
	});
}

function setBase64(){
	Base64={_keyStr:"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",encode:function(e){var t="";var n,r,i,s,o,u,a;var f=0;e=Base64._utf8_encode(e);while(f<e.length){n=e.charCodeAt(f++);r=e.charCodeAt(f++);i=e.charCodeAt(f++);s=n>>2;o=(n&3)<<4|r>>4;u=(r&15)<<2|i>>6;a=i&63;if(isNaN(r)){u=a=64}else if(isNaN(i)){a=64}t=t+this._keyStr.charAt(s)+this._keyStr.charAt(o)+this._keyStr.charAt(u)+this._keyStr.charAt(a)}return t},decode:function(e){var t="";var n,r,i;var s,o,u,a;var f=0;e=e.replace(/[^A-Za-z0-9\+\/\=]/g,"");while(f<e.length){s=this._keyStr.indexOf(e.charAt(f++));o=this._keyStr.indexOf(e.charAt(f++));u=this._keyStr.indexOf(e.charAt(f++));a=this._keyStr.indexOf(e.charAt(f++));n=s<<2|o>>4;r=(o&15)<<4|u>>2;i=(u&3)<<6|a;t=t+String.fromCharCode(n);if(u!=64){t=t+String.fromCharCode(r)}if(a!=64){t=t+String.fromCharCode(i)}}t=Base64._utf8_decode(t);return t},_utf8_encode:function(e){e=e.replace(/\r\n/g,"\n");var t="";for(var n=0;n<e.length;n++){var r=e.charCodeAt(n);if(r<128){t+=String.fromCharCode(r)}else if(r>127&&r<2048){t+=String.fromCharCode(r>>6|192);t+=String.fromCharCode(r&63|128)}else{t+=String.fromCharCode(r>>12|224);t+=String.fromCharCode(r>>6&63|128);t+=String.fromCharCode(r&63|128)}}return t},_utf8_decode:function(e){var t="";var n=0;var r=c1=c2=0;while(n<e.length){r=e.charCodeAt(n);if(r<128){t+=String.fromCharCode(r);n++}else if(r>191&&r<224){c2=e.charCodeAt(n+1);t+=String.fromCharCode((r&31)<<6|c2&63);n+=2}else{c2=e.charCodeAt(n+1);c3=e.charCodeAt(n+2);t+=String.fromCharCode((r&15)<<12|(c2&63)<<6|c3&63);n+=3}}return t}};
}

/**************************************************Funciones Genericas************************************************/
/*********************************************************************************************************************/