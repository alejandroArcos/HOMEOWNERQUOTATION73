$( document ).ready( function() {
	showLoader();
	window.scrollTo( 0, 0 );
	generaInfoCot();
	$( "#tabs" ).tabs();
	validaErrorCotizacion();
	aplicaReglas();
	seleccionaUltimaTab();
	hideLoader();
} );

function seleccionaUltimaTab(){
	var ultima = $("#tabs .ui-tabs-nav li").last();

	infoUltimaTab.objeto = ultima;
	infoUltimaTab.numero = ultima.index();
	infoUltimaTab.etiqueta = $(ultima).find(".numUbicacionEndo").text();
	
	 $( "#tabs" ).tabs( "option", "active", infoUltimaTab.numero );

}

function validaErrorCotizacion(){
	if(infoCotJson.noUbicaciones == 0){
		showMessageError( '.navbar', 'La información de las ubicaciones se encuentra vacía', 0 );
		/*regresaPaso1();*/
	}
}

function generaInfoCot() {
	if (!valIsNullOrEmpty( infCotString )) {
		infoCotJson = JSON.parse( infCotString );
		generaInfoAsegurados();
	}
}

function generaInfoAsegurados(){
	
	for(var index  = 0; index < infoCotJson.noUbicaciones; index++){
		contadorAsegurados.push(0);
		contadorBeneficiarios.push(0);
		idConsecutivoAseg.push(0);
 		idConsecutivoBenef.push(0);
	}
	
	generaPantallaAsegurados();
	generaPantallaBeneficiarios();
}

function aplicaReglas() {
	validaTipoMoneda();
	validaTipoUsoFamiliar();
	aplicaFiltros();
	seleccionaModo();
	validaRestriccionesSuscripcion();
	aplicaReglasEdoCotizacion();
	validaCamposCanalNegocio();
	validaCampoNivelRiesgo();
}

function validaCamposCanalNegocio() {
	var auxInfoP1 = JSON.parse(infoP1);
	
	$.each($(".suscriptor"), function(key, index){
	    $(this).parent().addClass('d-none');
	});
	
	if(auxInfoP1.canalNegocio == 2525 || auxInfoP1.canalNegocio == 2524 || auxInfoP1.canalNegocio == 6495 || auxInfoP1.canalNegocio == 6494) {
		$.each($(".suscriptor"), function(key, index){
		    $(this).parent().removeClass('d-none');
		});
	}
}

function validaCampoNivelRiesgo() {
	
	$.each($(".ubicacion"), function(key, value){
		
		var auxIdUbi = $(this).attr('id');
		
		$("#" + auxIdUbi + " #tip_inm-" + key).change();
	})
	
}

function generaPantallaAsegurados(){
	
	for(var auxContUbiAseg = 0; auxContUbiAseg < asegurados.length; auxContUbiAseg++){
		/*contadorAsegurados[auxContAseg] = asegurados[auxContAseg].length;*/
		var auxAsegurados = asegurados[auxContUbiAseg];
		
		for(var auxContAseg = 0; auxContAseg < asegurados[auxContUbiAseg].length; auxContAseg++){
			var auxUbiId = "ubicacion-" + (auxContUbiAseg + 1);
			
			var existe = validaPersonaExistente(auxContUbiAseg + 1, 'rowAsegurado', auxAsegurados[auxContAseg]);
			
			if(existe < 1) {
				agregaPersonaLleno(auxUbiId, auxAsegurados[auxContAseg], 'aa');
			}
		}
		
		if(contadorAsegurados[auxContUbiAseg] > 0){
			$("#checkAsegurado-" + auxContUbiAseg).attr('checked', true);
		}
	}
}

function generaPantallaBeneficiarios(){
	
	for(var auxContUbiBenef = 0; auxContUbiBenef < beneficiarios.length; auxContUbiBenef++){
		/*contadorAsegurados[auxContAseg] = asegurados[auxContAseg].length;*/
		var auxBeneficiarios = beneficiarios[auxContUbiBenef];
		
		for(var auxContBenef = 0; auxContBenef < beneficiarios[auxContUbiBenef].length; auxContBenef++){
			var auxUbiId = "ubicacion-" + (auxContUbiBenef + 1);
			var existe = validaPersonaExistente(auxContUbiBenef + 1, 'rowBeneficiario', auxBeneficiarios[auxContBenef]);
			
			if(existe < 1) {
				agregaPersonaLleno(auxUbiId, auxBeneficiarios[auxContBenef], 'bp');
			}
		}
		
		if(contadorBeneficiarios[auxContUbiBenef] > 0){
			$("#checkBeneficiario-" + auxContUbiBenef).attr('checked', true);
		}
	}	
}

function validaPersonaExistente(numUbi, auxTipoRow, valor) {
	
	var auxCoincidencias = 0;
	
	$.each($("#ubicacion-" + numUbi + " ." + auxTipoRow), function(keyR, valueR){
		
		if($(valueR).find('.rfc').attr('idPersona') == valor.idPersona && valor.idPersona != 0 && $(valueR).find('.rfc').attr('idPersona') != 0){
			auxCoincidencias++;
		}
	});
	
	return auxCoincidencias;
}

function validaTipoMoneda() {
	$.each( $( ".acordeon .moneda" ), function(key, registro) {
		daFormatoMoneda( registro );
	} );
}

function validaTipoUsoFamiliar() {
	if (infoCotJson.tipoCotizacion == tipoCotizacion.FAMILIAR) {
		$.each( $( "#tabs .ubicacion" ), function(key, registro) {
			filtraTipUsoPorUbicacion( "#" + $( registro ).attr( "id" ) );
			validaMedidasSeguridad("#" + $( registro ).attr( "id" ));
		} );
	}
}

function validaMedidasSeguridad(ubicacion) {
	
	var auxIdUbicacion = parseInt(ubicacion.split("-")[1])-1;
	
	$("#dr_descMedidasSeguridad-" + auxIdUbicacion).change();
}

function filtraTipUsoPorUbicacion(ubicacion) {
	$( ubicacion + " .acordeon div" ).not($(".suscriptor").parent()).removeClass( "d-none" );
	var tpoUso = $( ubicacion + " #tip_uso-" + (parseInt(ubicacion.split("-")[1])-1) ).val();
	console.log( "tpoUso : " + tpoUso );
	switch (tpoUso) {
		case "225":
			$( ubicacion + " .acordeon #PFCAMPCONTENIDOSDIV" ).addClass( "d-none" );
			$( ubicacion + " .acordeon #PFCAMPCONTENIDOSDIV :input" ).val('');
			$( ubicacion + " .acordeon #PFCAMPCONTENIDOSDIV" ).addClass( "d-none" );
			$( ubicacion + " .acordeon #PFCAMPCONTENIDOSDIV :input" ).val('');
			$( ubicacion + " .acordeon #PFCAMPRBSECIDIV" ).addClass( "d-none" );
			$( ubicacion + " .acordeon #PFCAMPRBSECIDIV :input" ).val('');
			$( ubicacion + " .acordeon #PFCAMPRBSECIIDIV" ).addClass( "d-none" );
			$( ubicacion + " .acordeon #PFCAMPRBSECIIDIV :input" ).val('');
			$( ubicacion + " .acordeon #PFCAMPRBSECIIIDIV" ).addClass( "d-none" );
			$( ubicacion + " .acordeon #PFCAMPRBSECIIIDIV :input" ).val('');
			$( ubicacion + " .acordeon #PFCAMPDINVALDIV" ).addClass( "d-none" );
			$( ubicacion + " .acordeon #PFCAMPDINVALDIV :input" ).val('');
			$( ubicacion + " .acordeon #PFCAMPEECDIV" ).addClass( "d-none" );
			$( ubicacion + " .acordeon #PFCAMPEECDIV :input" ).val('');
			$( ubicacion + " .acordeon #PFCAMPEEMDIV" ).addClass( "d-none" );
			$( ubicacion + " .acordeon #PFCAMPEEMDIV :input" ).val('');
			break;
		case "226":
			$( ubicacion + " .acordeon #PFCAMPPERDIDARDIV" ).addClass( "d-none" );
			$( ubicacion + " .acordeon #PFCAMPPERDIDARDIV :input" ).val('');
			break;
		case "1009":
			$( ubicacion + " .acordeon #PFCAMPEDIFICIODIV" ).addClass( "d-none" );
			$( ubicacion + " .acordeon #PFCAMPEDIFICIODIV :input" ).val('');
			$( ubicacion + " .acordeon #PFCAMPPERDIDARDIV" ).addClass( "d-none" );
			$( ubicacion + " .acordeon #PFCAMPPERDIDARDIV :input" ).val('');
			break;
		default:
			$( ubicacion + " .acordeon div" ).removeClass( "d-none" );
			$( ubicacion + " .acordeon div :input" ).val('');
			break;
	}
}

function filtraTipInmPorUbicacion(ubicacion) {
	
	var tpoInm = $( ubicacion + " #tip_inm-" + (parseInt(ubicacion.split("-")[1])-1) ).val();
	console.log( "tpoInm : " + tpoInm );
	
	switch(tpoInm) {
		case "224":
			$( ubicacion + " #dr_nivelRiesgo-" + (parseInt(ubicacion.split("-")[1])-1)).parent().removeClass('d-none');
			$( ubicacion + " #dr_nivelRiesgo-" + (parseInt(ubicacion.split("-")[1])-1)).addClass('infReq');
			break;
		default:
			$( ubicacion + " #dr_nivelRiesgo-" + (parseInt(ubicacion.split("-")[1])-1)).parent().addClass('d-none');
			$( ubicacion + " #dr_nivelRiesgo-" + (parseInt(ubicacion.split("-")[1])-1)).removeClass('infReq');
			break;
	}
	
}

function aplicaFiltros() {
	verificarVisibilidad();
	verificarDisabled();
	setObservableElements();
}

$( "#tabs .tip_uso" ).change( function() {
	var ubicacionPadre = $( this ).closest( ".ubicacion" );
	filtraTipUsoPorUbicacion( "#" + $( ubicacionPadre ).attr( "id" ) );
} );

$( "#tabs .tip_inm" ).change( function() {
	var ubicacionPadre = $( this ).closest( ".ubicacion" );
	filtraTipInmPorUbicacion( "#" + $( ubicacionPadre ).attr( "id" ) );
} );

$(".descMedidasSeguridad").change(function(){
	
	console.log('Entre');
	
	var ubicacionPadre = $( this ).closest( ".ubicacion" );
	var auxId = parseInt(ubicacionPadre.attr('id').split('-')[1]) - 1;
	
	if($(this).val() == "6199") {
		$("#" + $( ubicacionPadre ).attr( "id" ) + " #dr_descOtros-" + auxId).parent().removeClass('d-none');
		$("#" + $( ubicacionPadre ).attr( "id" ) + " #dr_descOtros-" + auxId).addClass('infReq');
	}
	else {
		$("#" + $( ubicacionPadre ).attr( "id" ) + " #dr_descOtros-" + auxId).parent().addClass('d-none');
		$("#" + $( ubicacionPadre ).attr( "id" ) + " #dr_descOtros-" + auxId).removeClass('infReq');
	}
});


$( '.dynamicAsegurado' ).on("click", '.tipo_persona_paso2_aa .form-check-input', function() {
	
	var auxIdRowAsegurado = $(this).closest('.rowAsegurado').attr('id');
	
	$('#' + auxIdRowAsegurado + ' .infoPersona').removeClass('d-none');
	
	var auxId = $(this).attr('id');
	var auxAsegurado = auxId.split("-");
	
	if ($( this ).val() == "1") {
		muestraCampos( "#" + auxAsegurado[1] + "-" + auxAsegurado[2] + " .tip_fisica_aa-" + auxAsegurado[3]);
		ocultaCampos( "#" + auxAsegurado[1] + "-" + auxAsegurado[2] + " .tip_moral_aa-" + auxAsegurado[3] );
		//fLlenaNombreFisica();
		
	} else {
		muestraCampos( "#" + auxAsegurado[1] + "-" + auxAsegurado[2] + " .tip_moral_aa-" + auxAsegurado[3] );
		ocultaCampos( "#" + auxAsegurado[1] + "-" + auxAsegurado[2] + " .tip_fisica_aa-" + auxAsegurado[3] );
		//fLlenaNombreMoral();
	}
	
	$(this).closest('.rowAsegurado').find('.infReq').removeClass('infReq');
	
} );

$( '.dynamicBeneficiario' ).on("click", '.tipo_persona_paso2_bp .form-check-input', function() {
	
	var auxIdRowBeneficiario = $(this).closest('.rowBeneficiario').attr('id');
	
	$('#' + auxIdRowBeneficiario + ' .infoPersona').removeClass('d-none');
	
	var auxId = $(this).attr('id');
	var auxAsegurado = auxId.split("-");
	
	if ($( this ).val() == "1") {
		muestraCampos( "#" + auxAsegurado[1] + "-" + auxAsegurado[2] + " .tip_fisica_bp-" + auxAsegurado[3]);
		ocultaCampos( "#" + auxAsegurado[1] + "-" + auxAsegurado[2] + " .tip_moral_bp-" + auxAsegurado[3] );
		//fLlenaNombreFisica();
		
	} else {
		muestraCampos( "#" + auxAsegurado[1] + "-" + auxAsegurado[2] + " .tip_moral_bp-" + auxAsegurado[3] );
		ocultaCampos( "#" + auxAsegurado[1] + "-" + auxAsegurado[2] + " .tip_fisica_bp-" + auxAsegurado[3] );
		//fLlenaNombreMoral();
	}
	
	$(this).closest('.rowBeneficiario').find('.infReq').removeClass('infReq');
} );


/*
$( '.tipo_persona_paso2_bp .form-check-input' ).click( function() {

	
	
	if ($( this ).val() == "1") {
		muestraCampos( ".tip_fisica_bp" );
		ocultaCampos( ".tip_moral_bp" );
		//fLlenaNombreFisica();
	} else {
		muestraCampos( ".tip_moral_bp" );
		ocultaCampos( ".tip_fisica_bp" );
		//fLlenaNombreMoral();
	}
} );
*/

function fLlenaNombreFisica() {
	var apPat = $( "#cn_fispaterno" ).val();
	var apMat = $( "#cn_fismaterno" ).val();
	var nom = $( "#cn_fisnombre" ).val();
	$( "#cn_nombrecompleto" ).val( nom + " " + apPat + " " + apMat );
	activaCampos( "#cn_nombrecompleto" );
}

function fLlenaNombreMoral() {
	var nom = ($( "#cn_nombrecontratante" ).val().length > 0) ? ($( "#cn_nombrecontratante" ).val() + ", ") : "";
	var tip = ($( "#cn_denominacion :selected" ).val() != '-1') ? $( "#cn_denominacion :selected" ).text() : "";
	$( "#cn_nombrecompleto" ).val( nom + tip );
	activaCampos( "#cn_nombrecompleto" );
}

function seleccionaModo() {
	switch (infoCotJson.modo) {
		case modo.NUEVA:
			$( "#btnCls1" ).addClass( "d-none" );
			break;
		case modo.EDICION:
			$( "#btnCls1" ).addClass( "d-none" );
			break;
		case modo.COPIA:
			$( "#btnCls1" ).addClass( "d-none" );
			break;
		case modo.ALTA_ENDOSO:
			$( "#btnCls1" ).addClass( "d-none" );
			break;
		case modo.EDITAR_ALTA_ENDOSO:
			$( "#btnCls1" ).addClass( "d-none" );
			break;
		case modo.CONSULTA:
			$( "#tabs button.close" ).addClass( "d-none" );
			$( "#btn-add-tab-endosos" ).addClass( "d-none" );
			$( "#save_tot2" ).addClass( "d-none" );
			$("#tabs :input, textarea, select").attr("disabled", true);
			$("#paso2_next").addClass("d-none");
			$("#paso2_next_paso3").removeClass("d-none");
			break;
		case modo.BAJA_ENDOSO:
			$( "#tabs button.close" ).addClass( "d-none" );
			$("#btn-add-tab-endosos").addClass( "d-none" );
			$("#paso1_back2").addClass( "d-none" );
			$("#chkBjaEnd").removeClass( "d-none" );
			$("#regresarEndoso").removeClass( "d-none" );
			$("#save_tot2").addClass("d-none");
			$("#infoPrimas").addClass("d-none");
			$("#paso2_next").addClass("d-none");
			$("#con_baja").removeClass("d-none");
			$("#contenPaso2 input, textarea, select ").not("#chkBjaEnd .form-check-input, #comentariosDosSuscrip").attr("disabled", true);
			break;
		case modo.EDITAR_BAJA_ENDOSO:
			$( "#tabs button.close" ).addClass( "d-none" );
			$("#btn-add-tab-endosos").addClass( "d-none" );
			$("#paso1_back2").addClass( "d-none" );
			$("#chkBjaEnd").removeClass( "d-none" );
			$("#regresarEndoso").removeClass( "d-none" );
			$("#save_tot2").addClass("d-none");
			$("#infoPrimas").addClass("d-none");
			$("#paso2_next").addClass("d-none");
			$("#con_baja").removeClass("d-none");
			$("#contenPaso2 input, textarea, select ").not("#chkBjaEnd .form-check-input, #comentariosDosSuscrip").attr("disabled", true);
			break;
		case modo.EDICION_JAPONES:
			bloqueaCamposEdicionJapones();
			break;
		case modo.CONSULTAR_REVISION:
			$( "#tabs button.close" ).addClass( "d-none" );
			$( "#btn-add-tab-endosos" ).addClass( "d-none" );
			$( "#save_tot2" ).addClass( "d-none" );
			$("#tabs :input, textarea, select").attr("disabled", true);
			$("#paso2_next").addClass("d-none");
			break;
		default:

			break;
	}
}

function bloqueaCamposEdicionJapones(){
	
}

$( "#btn-add-tab-endosos" ).click( function(e) {
	showLoader();
	e.preventDefault();
	if(validaNoTabs()){
		var faltaInfo = infoFaltanteUbicaciones();
		
		if(erroresCargaMasiva >= 20 && infoCargaMasiva) {
			showMessageError( '.navbar', "Los errores son demasiados para corregir manualmente, favor de regresar al paso 1, corregir el archivo y cargarlo nuevamente", 0 );
			erroresCargaMasiva = 0;
			hideLoader();
		}
		else {
			if (faltaInfo.code == 0) {
				infoCotJson.noUbicaciones += 1;
				llenaObjUbicacion( tipoGuardar.AGREGAR_UBICACION, NoEsELiminar );
				enviaUbicaciones();
			} else {
				showMessageError( '.navbar', faltaInfo.msg, 0 );
				erroresCargaMasiva = 0;
				hideLoader();
			}
		}
	}else{
		 showMessageError('.navbar', 'No se pueden agragar más ubicaciones', 0);
		 hideLoader();
	}
} );

function validaNoTabs(){
	if($.isNumeric( infoUltimaTab.etiqueta )){
		if(infoCotJson.tipoCotizacion == tipoCotizacion.EMPRESARIAL){
			if(parseInt(infoUltimaTab.etiqueta) < maxUbicaciones.empresarial){
				return true;
			}
		}
		if(infoCotJson.tipoCotizacion == tipoCotizacion.FAMILIAR){
			if(parseInt(infoUltimaTab.etiqueta) < maxUbicaciones.familiar){
				return true;
			}
		}
	}
	return false;
}

$( "#save_tot2" ).click( function(e) {
	showLoader();
	e.preventDefault();
	var faltaInfo = infoFaltanteUbicaciones();
	
	if(erroresCargaMasiva >= 20 && infoCargaMasiva) {
		showMessageError( '.navbar', "Los errores son demasiados para corregir manualmente, favor de regresar al paso 1, corregir el archivo y cargarlo nuevamente", 0 );
		erroresCargaMasiva = 0;
		hideLoader();
	}
	else{
		if (faltaInfo.code == 0) {
			llenaObjUbicacion( tipoGuardar.GUARDAR_UBICACION, NoEsELiminar );
			enviaUbicaciones();
		} else {
			showMessageError( '.navbar', faltaInfo.msg, 0 );
			hideLoader();
			erroresCargaMasiva = 0;
		}
	}
} );
/*
$( "#paso2_next" ).click( function(e) {
	showLoader();
	e.preventDefault();
	var faltaInfo = infoFaltanteUbicaciones();
	if (faltaInfo.code == 0) {
		continuar = true;
		llenaObjUbicacion( tipoGuardar.GUARDAR_UBICACION, NoEsELiminar );
		enviaUbicaciones();
	} else {
		showMessageError( '.navbar', faltaInfo.msg, 0 );
		hideLoader();
	}
} );
*/

var auxcontinue = 1;


/*
$("#form-paso2").submit(function(e){
	showLoader();
	console.log("submit form paso 2");
	
	if(auxcontinue == 1){
		$("#infoCotizacion").val("Zm9saW89MTExNjYwJmNvdGl6YWNpb249NTcwMSZ2ZXJzaW9uPTEmbW9kbz1FRElDSU9OJnRpcG9Db3RpemFjaW9uPUZBTUlMSUFSJnRpcG9QZXJzb25hPU1PUkFMJnBhbnRhbGxhPXBhcXVldGVGYW1pbGlhciZub1ViaWNhY2lvbmVzPTAmcG9saXphPW51bGw=");
		return;
	}
	
	e.preventDefault();
	hideLoader();
	
	
});
*/

$("#paso2_next_paso3").click(function(e) {
	
	$.post( redirigeURL, {
		infoCot : JSON.stringify( infoCotJson ),
		paso : "/paso3"
	} ).done( function(data) {
		var response = JSON.parse( data );

		$("#paso2-form-next #infoCotizacion").attr('disabled', false);
		$("#paso2-form-next #infoCotizacion").val(response.msg);
		$("#paso2-form-next").submit();			
	} );
	
});

$("#paso2_next").click(function(e){
	
	e.preventDefault();
	showLoader();
	
	var faltaInfo = infoFaltanteUbicaciones();
	
	if(erroresCargaMasiva >= 20 && infoCargaMasiva) {
		showMessageError( '.navbar', "Los errores son demasiados para corregir manualmente, favor de regresar al paso 1, corregir el archivo y cargarlo nuevamente", 0 );
		erroresCargaMasiva = 0;
		hideLoader();
	}
	else {
		if (faltaInfo.code == 0) {
			continuar = true;
			
			llenaObjUbicacion( tipoGuardar.GUARDAR_UBICACION, NoEsELiminar );
			
			var axinfp1 = JSON.parse(infoP1);
			/*
			if(axinfp1.subEstado == "CPS"){
				redirigeP3();
			}
			else { */
					$.post( guardaUbicacionURL, {
						listaUbicaciones : JSON.stringify( listaUbicaciones ),
						infCotString : JSON.stringify( infoCotJson )
					} ).done( function(data) {
						var response = JSON.parse( data );
						if (response.code == 0) {
							if(subGiroRiesgo != '1'){
								$("#paso2_next").removeClass("d-none");
								$("#infoPrimas").removeClass("d-none");
								$("#btnsuscontinuar").addClass("d-none");				
							}
							if (continuar) {
								$.post( redirigeURL, {
									infoCot : JSON.stringify( infoCotJson ),
									paso : "/paso3"
								} ).done( function(data) {
									var response = JSON.parse( data );
									if (response.code == 0) {
										if((axinfp1.canalNegocio == 2525 || axinfp1.canalNegocio == 6495) && perfilSuscriptor != 1) {
											hideLoader();
											$("#paso2_next").addClass("d-none");
											$("#infoPrimas").addClass("d-none");
											$("#btnsuscontinuar").addClass("d-none");
											$("#suscripMontoExedeTxt").text("LA SUMA ASEGURADA EXCEDE LOS LIMITES DE TU PERFIL ¿REQUIERES QUE EL NEGOCIO LO RETOME SUSCRIPCION?");
											$( '#modalSuscripMontoExede' ).modal( {
												show : true
											} );
											continuar = false;
										}
										else{
											var auxTechos = false;
											
											$.each($("select.tipoTechos"), function(){
											    auxTechos = $(this).val() == "6196" ? true : false;
											});
											
											if(auxTechos && (perfilJapones != 1 && perfilSuscriptor != 1)){
												hideLoader();
												$("#paso2_next").addClass("d-none");
												$("#infoPrimas").addClass("d-none");
												$("#btnsuscontinuar").addClass("d-none");
												$("#suscripMontoExedeTxt").text("LA SUMA ASEGURADA EXCEDE LOS LIMITES DE TU PERFIL ¿REQUIERES QUE EL NEGOCIO LO RETOME SUSCRIPCION?");
												$( '#modalSuscripMontoExede' ).modal( {
													show : true
												} );
												continuar = false;
												
											}
											else{
												
												$("#paso2-form #infoCotizacion").attr('disabled', false);
												$("#infoCotizacion").val(response.msg);
												$("#paso2-form").submit();
											}
										}
									} else {
										showMessageError( '.navbar', response.msg, 0 );
										hideLoader();
									}
								} );
							} else {
								location.reload();
							}
						}
						else {
							if(response.code == 5){
								$("#paso2_next").addClass("d-none");
								$("#infoPrimas").addClass("d-none");
								$("#btnsuscontinuar").addClass("d-none");
								$("#suscripMontoExedeTxt").text(response.msg);
								$( '#modalSuscripMontoExede' ).modal( {
									show : true
								} );
							}
							else {
								showMessageError( '.navbar', response.msg, 0 );				
							}
							continuar = false;
							hideLoader();
						}
					});
				/*
				}
				*/
			
		} else {
			showMessageError( '.navbar', faltaInfo.msg, 0 );
			hideLoader();
			erroresCargaMasiva = 0;
		}
	}
});

function enviaUbicaciones() {
	var axinfp1 = JSON.parse(infoP1);
	/*
	if(axinfp1.subEstado == "CPS"){
		redirigeP3();
	}else{
	*/
		$.post( guardaUbicacionURL, {
			listaUbicaciones : JSON.stringify( listaUbicaciones ),
			infCotString : JSON.stringify( infoCotJson )
		} ).done( function(data) {
			var response = JSON.parse( data );
			if (response.code == 0) {
				if(subGiroRiesgo != '1'){
					$("#paso2_next").removeClass("d-none");
					$("#infoPrimas").removeClass("d-none");
					$("#btnsuscontinuar").addClass("d-none");				
				}
				if (continuar) {
					redirigeP3();
				} else {
					location.reload();
				}
			} else {
				if(response.code == 5){
					
					
					$("#paso2_next").addClass("d-none");
					$("#infoPrimas").addClass("d-none");
					$("#btnsuscontinuar").addClass("d-none");
					$("#suscripMontoExedeTxt").text(response.msg);
					$( '#modalSuscripMontoExede' ).modal( {
						show : true
					} );
					
					
					
				}else{
					showMessageError( '.navbar', response.msg, 0 );				
				}
				continuar = false;
				hideLoader();
			}
	
		} );
	/*
	}
	*/
}

$(".dynamicAsegurado, .dynamicBeneficiario").on('focus', '.rfc', function(){
	
	var auxTipo = $(this).closest('.rowAsegurado, .rowBeneficiario').parent().attr('class');
	var auxIdRow = $(this).closest('.rowAsegurado, .rowBeneficiario').attr('id');
	var auxId = auxIdRow.charAt(auxIdRow.length-1);
	var auxIdUbi = $(this).closest('.ubicacion').attr('id');
	
	var auxPrefijo = "";
	
	switch(auxTipo) {
		case 'dynamicAsegurado':
			auxPrefijo = "aa";
			break;
		case 'dynamicBeneficiario':
			auxPrefijo = "bp";
			break;
	}
	
	$(this).autocomplete( {
		minLength : 3,
		source : function(request, response) {
			$.getJSON( ligasServicios.listaPersonas, {
				term : request.term,
				tipo : 3,
				pantalla : infoCotJson.pantalla
			}, function(data, status, xhr) {
				sessionExtend();
				if (data.codigo == '0') {
					showMessageError( '.navbar', msj.es.errorInformacion, 0 );
					console.error("autocomplete rfc");
					response( null );
				} else {
					response( data );
				}
			} );
		},
		focus : function(event, ui) {
			$( "#ce_rfc" ).val( ui.item.nombrepersona );
			return false;
		},
		select : function(event, ui) {
			/*
			switch(ui.item.tipoPer) {
				case 217:
					$("#radio_pf_" + auxPrefijo + "-" + auxIdUbi + "-" + auxId).click();
					$( "#" + auxPrefijo + "_fisnombre-" + auxId ).val( ui.item.nombre );
					$( "#" + auxPrefijo + "_fispaterno-" + auxId ).val( ui.item.appPaterno );
					$( "#" + auxPrefijo + "_fismaterno-" + auxId ).val( ui.item.appMaterno );
					activaCampos("#" + auxPrefijo + "_fisnombre-" + auxId);
					activaCampos("#" + auxPrefijo + "_fispaterno-" + auxId);
					activaCampos("#" + auxPrefijo + "_fismaterno-" + auxId);
					break;
				case 218:
					$("#radio_pm_" + auxPrefijo + "-" + auxIdUbi + "-" + auxId).click();
					$( "#" + auxPrefijo + "_nombrecontratante-" + auxId ).val( ui.item.nombre.split(",")[0] );
					activaCampos("#" + auxPrefijo + "_nombrecontratante-" + auxId);
					$( "#" + auxPrefijo + "_denominacion-" + auxId ).val( ui.item.idDenominacion );
					$( "#" + auxPrefijo + "_denominacion-" + auxId ).materialSelect();
					break;
			}
			*/
			$( "#" + auxIdUbi + " #" + auxPrefijo + "_rfc-" + auxId ).val( ui.item.rfc );
			
			/*
			
			$( "#" + auxPrefijo + "_rfc-" + auxId ).attr('codigo', ui.item.codigo);
			$( "#" + auxPrefijo + "_rfc-" + auxId ).attr('idPersona', ui.item.idPersona);
		
			auxP1.infoClientExistenttEncontrado = ui.item;
			//seleccionaTipoPer();
			*/
			return false;
		},
		error : function(jqXHR, textStatus, errorThrown) {
			showMessageError( '.navbar', msj.es.errorInformacion, 0 );
			console.error("autocomplete rfc");
		}
	} ).autocomplete( "instance" )._renderItem = function(ul, item) {
		if (item.idDenominacion == 0) {
			return $( "<li>" ).append(
					"<div>" + item.rfc + " - " + item.nombre + " " + item.appPaterno + " " + item.appMaterno
							+ "</div>" ).appendTo( ul );
		} else {
			return $( "<li>" ).append(
					"<div>" + item.rfc + " - " + item.nombre + " " + item.appPaterno + " " + item.appMaterno
							+ "</div>" ).appendTo( ul );
		}
	};
	
});

$(".dynamicAsegurado, .dynamicBeneficiario").on('blur', '.rfc', function(){
	
	var auxTipo = $(this).closest('.rowAsegurado, .rowBeneficiario').parent().attr('class');
	var auxIdRow = $(this).closest('.rowAsegurado, .rowBeneficiario').attr('id');
	var auxId = auxIdRow.charAt(auxIdRow.length-1);
	var auxIdUbi = $(this).closest('.ubicacion').attr('id');
	var auxPrefijo = "";
	var vecesRfc = 0;
	var auxRfc = $(this).val();
	var auxClaseRadio = "";
	var auxClaseNombre = "";
	
	/*
	if(!validaFormatoRfc(auxRfc) && auxRfc != "") {
		showMessageError('.navbar', 'El RFC no cumple con el formato, favor de introducirlo nuevamente', 0);
		$(this).val('');
		$(this).focus();
		return;
	}
	*/
	
	if (rfcGenerico.indexOf( auxRfc ) < 0) {
		$.each($("#" + auxIdUbi + " ." + auxTipo +" .rfc"), function(key, value){
			if($(this).val() == auxRfc){
				vecesRfc++;
			}
		});
	}
	
	if(vecesRfc > 1){
		showMessageError('.navbar', 'El RFC ya fue introducido en esta sección, favor de introducir uno nuevo', 0);
		$(this).val('');
		$(this).focus();
		return;
	}
	
	
	var auxTipoRow = "";
	
	switch(auxTipo) {
		case 'dynamicAsegurado':
			auxTipoRow = "rowAsegurado";
			auxPrefijo = "aa";
			auxClaseRadio = "Asegurado";
			break;
		case 'dynamicBeneficiario':
			auxTipoRow = "rowBeneficiario";
			auxPrefijo = "bp";
			auxClaseRadio = "Beneficiario";
			break;
	}
	
	
	
	if(!valIsNullOrEmpty($(this).val())){
		showLoader();
		auxP1.infoClientExistenttEncontrado = null;
		var rfc = $( this ).val().toUpperCase();
		if (rfcGenerico.indexOf( rfc ) < 0) {
			$.post( ligasServicios.listaPersonas, {
				term : $( this ).val(),
				tipo : 3,
				pantalla : infoCotJson.pantalla
			}, function(data) {
				var response = jQuery.parseJSON( data );
				if (response.length > 0) {
					$.each( response, function(key, registro) {
						if (registro.rfc === rfc) {
							auxP1.infoClientExistenttEncontrado = registro;
							auxP1.auxPrefijo = auxPrefijo;
							auxP1.auxId = auxId;
							auxP1.auxIdUbi = auxIdUbi;
							return false;
						}
					} );
					if (auxP1.infoClientExistenttEncontrado != null) {
						$( '#nombreClienteExistt' ).text(
								auxP1.infoClientExistenttEncontrado.rfc + ' - ' + auxP1.infoClientExistenttEncontrado.nombre
										+ ' ' + auxP1.infoClientExistenttEncontrado.appPaterno + ' '
										+ auxP1.infoClientExistenttEncontrado.appMaterno );
						$( '#modalClienteExistente' ).modal( 'show' );
					}
				}else{
					auxP1.infoClientExistenttEncontrado = null;
				}
				hideLoader();
			} );
		}else{
			hideLoader();
		}
		
		auxClaseNombre = $("input[name='group" + auxClaseRadio + "-" + auxIdUbi + "-" + auxId + "']:checked").val() == 0 ? "nombreM" : "nombreF";
		
		$("#" + auxIdUbi + " #" + auxIdRow + " ." + auxClaseNombre).addClass('infReq');
	}
	
});

$(".dynamicAsegurado, .dynamicBeneficiario").on('blur', '.nombreM', function(){

	$(this).closest('.rowAsegurado, .rowBeneficiario').find('.tipoS').addClass('infReq');

});

$(".dynamicAsegurado, .dynamicBeneficiario").on('blur', '.nombreF', function(){

	$(this).closest('.rowAsegurado, .rowBeneficiario').find('.apellidoP').addClass('infReq');

});


$(".dynamicAsegurado, .dynamicBeneficiario").on('blur', '.nombreF, .apellidoP, .apellidoM, .nombreM', function(){
	
	var auxRfc = $(this).closest('.rowAsegurado, .rowBeneficiario').find('.rfc').val();
	var auxTipoP = $(this).closest('.rowAsegurado, .rowBeneficiario').find("input[type='radio']:checked").val();
	
	var auxNombre = "";
	var auxSociedad = 0;
	var auxNombreF = "";
	var auxAP = "";
	var auxAM = "";
	
	switch(auxTipoP) {
		case "0":	
				auxNombre = $(this).closest('.rowAsegurado, .rowBeneficiario').find('.nombreM').val();
				auxSociedad = $(this).closest('.rowAsegurado, .rowBeneficiario').find('select.tipoS').val();
				break;
		case "1":	
				auxNombreF = $(this).closest('.rowAsegurado, .rowBeneficiario').find('.nombreF').val();
				auxAP = $(this).closest('.rowAsegurado, .rowBeneficiario').find('.apellidoP').val();
				auxAM = $(this).closest('.rowAsegurado, .rowBeneficiario').find('.apellidoM').val();
				break;
	}
	
	if(!(rfcGenerico.indexOf(auxRfc) < 0) || valIsNullOrEmpty(auxRfc)) {
		
		var auxIdUbi = $(this).closest('.ubicacion').attr('id');
		var auxCoincidenciasExactas = 0;
	
		$.each($("#" + auxIdUbi + " .dynamicAsegurado .rowAsegurado, #" + auxIdUbi + " .dynamicBeneficiario .rowBeneficiario"), function(key, value) {
			console.log(this);	
			
			var auxRfcPersona = $(this).find('.rfc').val();
			
			if(!(rfcGenerico.indexOf(auxRfcPersona) < 0) || valIsNullOrEmpty(auxRfcPersona)) {
				
				var auxTipoPersona = $(this).find("input[type='radio']:checked").val();
				
				if(auxTipoP == auxTipoPersona) {
					switch(auxTipoPersona) {
						case "0":	
								var auxNombreMP = $(this).find('.nombreM').val();
								var auxSociedadP = $(this).find('select.tipoS').val();
								
								if(auxNombre == auxNombreMP && auxSociedadP == auxSociedad) {
									auxCoincidenciasExactas++;
								}
								
								break;
						case "1":	
								var auxNombreFP = $(this).find('.nombreF').val();
								var auxAPP = $(this).find('.apellidoP').val();
								var auxAMP = $(this).find('.apellidoM').val();
								
								if(auxNombreFP == auxNombreF && auxAPP == auxAP && auxAMP == auxAM) {
									auxCoincidenciasExactas++;
								}
								
								break;
					}
				}
			}
		});
		
		if(auxCoincidenciasExactas > 1) {
			showMessageError('.navbar', 'Error de duplicados', 0);
			$(this).closest('.rowAsegurado, .rowBeneficiario').find('input:not([type=radio])').val('');
			$(this).closest('.rowAsegurado, .rowBeneficiario').find('select').val(-1).materialSelect();
		}
	}

});

$(".dynamicAsegurado, .dynamicBeneficiario").on('change', '.tipoS', function(){
	
	var auxRfc = $(this).closest('.rowAsegurado, .rowBeneficiario').find('.rfc').val();
	var auxTipoP = $(this).closest('.rowAsegurado, .rowBeneficiario').find("input[type='radio']:checked").val();
	
	var auxNombre = "";
	var auxSociedad = 0;
	var auxNombreF = "";
	var auxAP = "";
	var auxAM = "";
	
	switch(auxTipoP) {
		case "0":	
				auxNombre = $(this).closest('.rowAsegurado, .rowBeneficiario').find('.nombreM').val();
				auxSociedad = $(this).closest('.rowAsegurado, .rowBeneficiario').find('select.tipoS').val();
				break;
		case "1":	
				auxNombreF = $(this).closest('.rowAsegurado, .rowBeneficiario').find('.nombreF').val();
				auxAP = $(this).closest('.rowAsegurado, .rowBeneficiario').find('.apellidoP').val();
				auxAM = $(this).closest('.rowAsegurado, .rowBeneficiario').find('.apellidoM').val();
				break;
	}
	
	if(!(rfcGenerico.indexOf(auxRfc) < 0) || valIsNullOrEmpty(auxRfc)) {
		
		var auxIdUbi = $(this).closest('.ubicacion').attr('id');
		var auxCoincidenciasExactas = 0;
	
		$.each($("#" + auxIdUbi + " .dynamicAsegurado .rowAsegurado, #" + auxIdUbi + " .dynamicBeneficiario .rowBeneficiario"), function(key, value) {
			console.log(this);	
			
			var auxRfcPersona = $(this).find('.rfc').val();
			
			if(!(rfcGenerico.indexOf(auxRfcPersona) < 0) || valIsNullOrEmpty(auxRfcPersona)) {
				
				var auxTipoPersona = $(this).find("input[type='radio']:checked").val();
				
				if(auxTipoP == auxTipoPersona) {
					switch(auxTipoPersona) {
						case "0":	
								var auxNombreMP = $(this).find('.nombreM').val();
								var auxSociedadP = $(this).find('select.tipoS').val();
								
								if(auxNombre == auxNombreMP && auxSociedadP == auxSociedad) {
									auxCoincidenciasExactas++;
								}
								
								break;
						case "1":	
								var auxNombreFP = $(this).find('.nombreF').val();
								var auxAPP = $(this).find('.apellidoP').val();
								var auxAMP = $(this).find('.apellidoM').val();
								
								if(auxNombreFP == auxNombreF && auxAPP == auxAP && auxAMP == auxAM) {
									auxCoincidenciasExactas++;
								}
								
								break;
					}
				}
			}
		});
		
		if(auxCoincidenciasExactas > 1) {
			showMessageError('.navbar', 'Error de duplicados', 0);
			$(this).closest('.rowAsegurado, .rowBeneficiario').find('input:not([type=radio])').val('');
			$(this).closest('.rowAsegurado, .rowBeneficiario').find('select').val(-1).materialSelect();
		}
	}

});

$( '#btnClienttExisttSi' ).click(function() {
	
	
	switch(auxP1.infoClientExistenttEncontrado.tipoPer) {
		case 217:
			$("#radio_pf_" + auxP1.auxPrefijo + "-" + auxP1.auxIdUbi + "-" + auxP1.auxId).click();
			$( "#" + auxP1.auxIdUbi + " #" + auxP1.auxPrefijo + "_fisnombre-" + auxP1.auxId ).val( auxP1.infoClientExistenttEncontrado.nombre );
			$( "#" + auxP1.auxIdUbi + " #" + auxP1.auxPrefijo + "_fispaterno-" + auxP1.auxId ).val( auxP1.infoClientExistenttEncontrado.appPaterno );
			$( "#" + auxP1.auxIdUbi + " #" + auxP1.auxPrefijo + "_fismaterno-" + auxP1.auxId ).val( auxP1.infoClientExistenttEncontrado.appMaterno );
			activaCampos("#" + auxP1.auxIdUbi + " #" + auxP1.auxPrefijo + "_fisnombre-" + auxP1.auxId);
			activaCampos("#" + auxP1.auxIdUbi + " #" + auxP1.auxPrefijo + "_fispaterno-" + auxP1.auxId);
			activaCampos("#" + auxP1.auxIdUbi + " #" + auxP1.auxPrefijo + "_fismaterno-" + auxP1.auxId);
			break;
		case 218:
			$("#radio_pm_" + auxP1.auxPrefijo + "-" + auxP1.auxIdUbi + "-" + auxP1.auxId).click();
			$( "#" + auxP1.auxIdUbi + " #" + auxP1.auxPrefijo + "_nombrecontratante-" + auxP1.auxId ).val( auxP1.infoClientExistenttEncontrado.nombre.split(",")[0] );
			activaCampos("#" + auxP1.auxIdUbi + " #" + auxP1.auxPrefijo + "_nombrecontratante-" + auxP1.auxId);
			$( "#" + auxP1.auxIdUbi + " #" + auxP1.auxPrefijo + "_denominacion-" + auxP1.auxId ).val( auxP1.infoClientExistenttEncontrado.idDenominacion );
			$( "#" + auxP1.auxIdUbi + " #" + auxP1.auxPrefijo + "_denominacion-" + auxP1.auxId ).materialSelect();
			break;
	}
	
	$( "#" + auxP1.auxIdUbi + " #" + auxP1.auxPrefijo + "_rfc-" + auxP1.auxId ).val( auxP1.infoClientExistenttEncontrado.rfc );
	$( "#" + auxP1.auxIdUbi + " #" + auxP1.auxPrefijo + "_rfc-" + auxP1.auxId ).attr('codigo', auxP1.infoClientExistenttEncontrado.codigo);
	$( "#" + auxP1.auxIdUbi + " #" + auxP1.auxPrefijo + "_rfc-" + auxP1.auxId ).attr('idPersona', auxP1.infoClientExistenttEncontrado.idPersona);
	
	
	$("#modalClienteExistente").modal("hide");
	
} );

$("#btnSuscripMontoSi").click(function(){
	showLoader();
	var aux = "LIFERAY_SHARED_F=" + infoCotJson.folio + "_C="
	+ infoCotJson.cotizacion + "_V=" + infoCotJson.version
	+ "_ACEPTASUSCRIPCION";
	var aux2 = "LIFERAY_SHARED_F=" + infoCotJson.folio + "_C="
	+ infoCotJson.cotizacion + "_V=" + infoCotJson.version
	+ "_EXCEDELIMITES";
	$.post( aceptaSuscripcionURL, {
		nombre : aux,
		nombre2 : aux2
	} ).done( function(data) {
		var response = JSON.parse( data );
		/*location.reload();*/
		excedeLimites = '1';
		aceptaSuscripcion = '1';
		fExcedeLimites();
		$('#modalSuscripMontoExede').modal('hide');	
		location.reload();
	});
});

function redirigeP3() {
	$.post( redirigeURL, {
		infoCot : JSON.stringify( infoCotJson ),
		paso : "/paso3"
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

function llenaObjUbicacion(tipoGuardar, ubicacionBorrar) {
	
	var infoP1Json = JSON.parse(infoP1);
	
	listaUbicaciones.p_cotizacion = infoCotJson.cotizacion;
	listaUbicaciones.p_version = infoCotJson.version;
	listaUbicaciones.p_tipoGuardar = tipoGuardar;
	listaUbicaciones.p_ubicacion = generainfoUbicaciones( ubicacionBorrar );
	listaUbicaciones.p_folio = infoCotJson.folio + "";
	listaUbicaciones.p_agente = infoP1Json.codigoAgente;

}

function generainfoUbicaciones(ubicacionBorrar) {
	var infUbicaciones = new Array();
	var totUbicaciones = $( ".numUbicacionEndo" ).length;

	for (var i = 0; i < totUbicaciones; i++) {
		if ((i + 1) != ubicacionBorrar) {
			var ub = new Object();
			ub.idUbicacion = $( $( ".numUbicacionEndo" )[i] ).text();
			ub.calle = $( "#ubicacion-" + (i + 1) + " #dr_calle-" + (i) ).val();
			ub.numero = $( "#ubicacion-" + (i + 1) + " #dr_numero-" + (i) ).val();
			ub.numeroInterior = $( "#ubicacion-" + (i + 1) + " #dr_numeroInt-" + (i) ).val();
			ub.cpData = getInfoCp( i + 1 );
			ub.niveles = $( "#ubicacion-" + (i + 1) + " #dr_nivel-" + (i) ).val();
			ub.valorInmueble = 0.0;
			ub.descripcionConstruccion = $( "#ubicacion-" + (i + 1) + " #dr_DescripcionConstruccion-" + (i) ).val();
			ub.tipoInmueble = $( "#ubicacion-" + (i + 1) + " #tip_inm-" + (i) ).val();
			ub.tipoUso = $( "#ubicacion-" + (i + 1) + " #tip_uso-" + (i) ).val();
			ub.aseguradoAdicional = generaAsegurados(i);
			ub.beneficiarioPreferente = generaBeneficiarios(i);
			ub.cercaDelMar = $( "#ubicacion-" + (i + 1) + " input[name=group-ubi-"+ (i + 1) +"]:checked").val() == 2 ? 1 : 0;
			ub.fields = generaFils( i + 1 );
			ub.techos = $( "#ubicacion-" + (i + 1) + " #dr_descTechos-" + (i) ).val();
			ub.muros = $( "#ubicacion-" + (i + 1) + " #dr_descMuros-" + (i) ).val();
			ub.medidasSeguridad = $( "#ubicacion-" + (i + 1) + " #dr_descMedidasSeguridad-" + (i) ).val();
			ub.descripcionMedidaSeguridad = $( "#ubicacion-" + (i + 1) + " #dr_descOtros-" + (i) ).val();
			ub.longitud = $( "#ubicacion-" + (i + 1) + " #dr_longitud-" + (i) ).val();
			ub.latitud = $( "#ubicacion-" + (i + 1) + " #dr_latitud-" + (i) ).val();
			ub.nivelRiesgo = $( "#ubicacion-" + (i + 1) + " #dr_nivelRiesgo-" + (i) ).val();

			infUbicaciones.push( ub );
		}
	}

	return infUbicaciones;
}

function getInfoCp(i) {
	var infoCp = new Object();
	infoCp.idCp = parseInt( $( "#ubicacion-" + i + " #dr_colonia-" + (i-1) ).val() );
	infoCp.cp = $( "#ubicacion-" + i + " #dr_cp-" + (i-1) ).val();
	infoCp.colonia = $( "#ubicacion-" + i + " #dr_colonia-" + (i-1) + " option:selected" ).text();
	infoCp.delegacion = $( "#ubicacion-" + i + " #dr_municipio-" + (i-1) ).val();
	infoCp.estado = $( "#ubicacion-" + i + " #dr_estado-" + (i-1) ).val();
	return infoCp;
}

function generaFils(i) {
	var fils = new Object();
	$.each( $( "#ubicacion-" + i + " .acordeon input" ), function(key, value) {
		if ($( value ).is( ":text" )) {
			if ($( value ).hasClass( "select-dropdown" )) {
				var sel = $( value ).siblings( "select" );
				if (valIsNullOrEmpty( $( sel ).val() )) {
					fils[$( sel ).attr( "name" )] = "";
				} else {
					fils[$( sel ).attr( "name" )] = $( sel ).val();
				}
			} else {
				fils[$( value ).attr( "name" )] = quitaTipoMoneda( $( value ).val() );
			}
		} else if ($( value ).is( ":checkbox" )) {
			fils[$( value ).attr( "name" )] = $( value ).is( ":checked" ) ? 1 : 0;
		}
	} );
	return fils;
}

function generaAsegurados(i) {
	
	var aseguradosUbiTemp = [];
	
	$.each( $( "#ubicacion-" + (i+1) + " .rowAsegurado" ), function(key, value){
		
		var auxId = $(this).attr('id').charAt($(this).attr('id').length - 1);
		
		var objetoAseguradoAux = new Object();
		
		objetoAseguradoAux.idPersona = $(this).find("#aa_rfc-" + auxId).attr('idPersona');
		objetoAseguradoAux.tipoPer = $("#radio_pm_aa-ubicacion-" + (i+1) + "-" + auxId).is(':checked') ? 218 : 217;
		objetoAseguradoAux.rfc = $(this).find("#aa_rfc-" + auxId).val() != "" ? $(this).find("#aa_rfc-" + auxId).val() : ($("#radio_pm_aa-ubicacion-" + (i+1) + "-" + auxId).is(':checked') ? rfcGenerico[0] : ($(this).find("#aa_chktoggle-" + auxId).is(':checked') ? rfcGenerico[1] : rfcGenerico[0]));
		objetoAseguradoAux.nombre = $("#radio_pm_aa-ubicacion-" + (i+1) + "-" + auxId).is(':checked') ? 
			$(this).find("#aa_nombrecontratante-" + auxId).val() : $(this).find("#aa_fisnombre-" + auxId).val()
		objetoAseguradoAux.appPaterno = $(this).find("#aa_fispaterno-" + auxId).val();
		objetoAseguradoAux.appMaterno = $(this).find("#aa_fismaterno-" + auxId).val();
		objetoAseguradoAux.idDenominacion = $(this).find("#aa_denominacion-" + auxId).val();
		objetoAseguradoAux.codigo = $(this).find("#aa_rfc-" + auxId).attr('codigo');
		objetoAseguradoAux.extranjero = $(this).find("#aa_chktoggle-" + auxId).is(':checked') ? 1 : 0;
		objetoAseguradoAux.aplicaUbicaciones = $(this).find("#aa_aplicaUbicaciones-" + auxId).is(':checked') ? 1 : 0;
		
		aseguradosUbiTemp.push(objetoAseguradoAux);
	});
	
	console.log(aseguradosUbiTemp);
	
	return aseguradosUbiTemp;
}

function generaBeneficiarios(i) {
	
	var beneficiariosUbiTemp = [];
	
	$.each( $( "#ubicacion-" + (i+1) + " .rowBeneficiario" ), function(key, value){
		
		var auxId = $(this).attr('id').charAt($(this).attr('id').length - 1);
		
		var objetoBeneficiarioAux = new Object();
		
		objetoBeneficiarioAux.idPersona = $(this).find("#bp_rfc-" + auxId).attr('idPersona');
		objetoBeneficiarioAux.tipoPer = $("#radio_pm_bp-ubicacion-" + (i+1) + "-" + auxId).is(':checked') ? 218 : 217;
		objetoBeneficiarioAux.rfc = $(this).find("#bp_rfc-" + auxId).val() != "" ? $(this).find("#bp_rfc-" + auxId).val() : ($("#radio_pm_bp-ubicacion-" + (i+1) + "-" + auxId).is(':checked') ? rfcGenerico[0] : ($(this).find("#bp_chktoggle-" + auxId).is(':checked') ? rfcGenerico[1] : rfcGenerico[0]));
		objetoBeneficiarioAux.nombre = $("#radio_pm_bp-ubicacion-" + (i+1) + "-" + auxId).is(':checked') ? 
			$(this).find("#bp_nombrecontratante-" + auxId).val() : $(this).find("#bp_fisnombre-" + auxId).val()
		objetoBeneficiarioAux.appPaterno = $(this).find("#bp_fispaterno-" + auxId).val();
		objetoBeneficiarioAux.appMaterno = $(this).find("#bp_fismaterno-" + auxId).val();
		objetoBeneficiarioAux.idDenominacion = $(this).find("#bp_denominacion-" + auxId).val();
		objetoBeneficiarioAux.codigo = $(this).find("#bp_rfc-" + auxId).attr('codigo');
		objetoBeneficiarioAux.extranjero = $(this).find("#bp_chktoggle-" + auxId).is(':checked') ? 1 : 0;
		objetoBeneficiarioAux.aplicaUbicaciones = $(this).find("#bp_aplicaUbicaciones-" + auxId).is(':checked') ? 1 : 0;
		
		beneficiariosUbiTemp.push(objetoBeneficiarioAux);
	});
	
	console.log(beneficiariosUbiTemp);
	
	return beneficiariosUbiTemp;
}

function quitaTipoMoneda(data) {
	return data.replace( /[$,]/g, '' );
}

function infoFaltanteUbicaciones() {
	$( ".alert-danger" ).remove();
	$( '.invalid' ).removeClass( 'invalid' );
	var totUbicaciones = $( ".numUbicacionEndo" ).length;
	var info = new Object();
	info.code = 0;
	info.msg = "";
	for (var i = 0; i < totUbicaciones; i++) {
		var errorPestania = false;
		$.each( $( "#ubicacion-" + (i + 1) + " input.infReq" ), function(key, value) {
			if (valIsNullOrEmpty( $( value ).val() )) {
				$( value ).addClass( 'invalid' );
				$( value ).parent().append(
						"<div class=\"alert alert-danger\" role=\"alert\"> <span class=\"glyphicon glyphicon-ban-circle\"></span>"
								+ " Hace falta información requerida </div>" );

				errorPestania = true;
				erroresCargaMasiva++;
			}
		} );

		$.each( $( "#ubicacion-" + (i + 1) + " select.infReq" ), function(key, value) {
			if ($( value ).val() == "-1") {
				$( value ).siblings( "input" ).addClass( 'invalid' );
				$( value ).parent().append(
						"<div class=\"alert alert-danger\"> <span class=\"glyphicon glyphicon-ban-circle\"></span> "
								+ "Hace falta información requerida </div>" );
				errorPestania = true;
				erroresCargaMasiva++;
			}
		} );
		
		if($("#ubicacion-" + (i + 1) + " input:radio.infReq:checked" ).length == 0) {
			
			$("#ubicacion-" + (i + 1) + " input:radio.infReq").parent().parent().append(
						"<div class=\"alert alert-danger\" role=\"alert\"> <span class=\"glyphicon glyphicon-ban-circle\"></span>"
								+ " Hace falta información requerida </div>" );
			
			errorPestania = true;
			erroresCargaMasiva++;
		}

		if (errorPestania) {
			info.code = 2;
			if (valIsNullOrEmpty( info.msg )) {
				info.msg = "Falta información requerida en la pestaña " + $( $( ".numUbicacionEndo" )[i] ).text();
			} else {
				info.msg += "<br/>Falta información requerida en la pestaña " + $( $( ".numUbicacionEndo" )[i] ).text();
			}

		}
	}
	return info;
}

$( ".cpValido" ).blur( function() {
	showLoader();
	cp = $( this ).val();
	var ubicacionPadre = $( this ).closest( ".ubicacion" )
	if ((cp.length < 5) && (cp.length > 0)) {
		$( this ).focus();
		showMessageError( '.navbar', 'Por favor verifique el código postal a 5 dígitos', 0 );
		hideLoader();
	} else if (cp.length == 5) {
		limpiaCamposCp( ubicacionPadre );
		llenaInfoByCP( ubicacionPadre, cp );
	} else {
		limpiaCamposCp( ubicacionPadre );
		showMessageSuccess( '.navbar', ' Código postal vacio', 0 );
		hideLoader();
	}
} );

function limpiaCamposCp(ubicacionPadre) {
	
	var auxIdUb = parseInt($(ubicacionPadre).attr('id').split("-")[1]) - 1;
	
	$( ubicacionPadre ).find( "#dr_calle-" + auxIdUb ).val( "" );
	$( ubicacionPadre ).find( "#dr_numero-" + auxIdUb ).val( "" );
	$( ubicacionPadre ).find( "#dr_municipio-" + auxIdUb ).val( "" );
	$( ubicacionPadre ).find( "#dr_estado-" + auxIdUb ).val( "" );
	$( ubicacionPadre ).find( "#dr_colonia-" + auxIdUb + " option:not(:first)" ).remove();
	selectDestroy( $( ubicacionPadre ).find( '#dr_colonia-' + auxIdUb  ), false );
}

function llenaInfoByCP(ubicacionPadre, cp) {
	$.post( getCpURL, {
		cp : cp,
		pantalla : infoCotJson.pantalla
	} ).done( function(data) {
		var response = JSON.parse( data );
		console.log( response );
		if (response.code == 0) {
			llenaInfoCp( ubicacionPadre, response.cpData );
		} else {
			if (response.code == 4) {
				
				$( "#suscripCpRiesgo" ).text( response.msg );
				$( '#modalSuscripCPRiesgo' ).modal( {
					show : true
				} );
			}
			$( ubicacionPadre ).find( "#dr_cp" ).val("");
			
			showMessageError( '.navbar', response.msg, 0 );
		}
		hideLoader();
	} );
}

function llenaInfoCp(ubicacionPadre, cpData) {
	var auxIdUb = parseInt($(ubicacionPadre).attr('id').split("-")[1]) - 1;
	$.each( cpData, function(key, value) {
		$( ubicacionPadre ).find( "#dr_cp-" + auxIdUb ).attr( "idcp", value.idCp );
		$( ubicacionPadre ).find( "#dr_municipio-" + auxIdUb ).val( value.delegacion );
		$( ubicacionPadre ).find( "#dr_municipio-" + auxIdUb ).siblings( "label" ).addClass( "active" );
		$( ubicacionPadre ).find( "#dr_estado-" + auxIdUb ).val( value.estado );
		$( ubicacionPadre ).find( "#dr_estado-" + auxIdUb ).siblings( "label" ).addClass( "active" );
		$( ubicacionPadre ).find( '#dr_colonia-' + auxIdUb ).append( new Option( value.colonia, value.idCp ) );
		selectDestroy( $( ubicacionPadre ).find( '#dr_colonia-' + auxIdUb ), false );
	} );
}

$( "#tabs button.close" ).click( function(e) {
	var liSelect = $( this ).closest( "li" );
	var ubica = $( liSelect ).attr( "aria-controls" );
	var sepUbica = ubica.split( "-" );

	if (sepUbica.length > 1) {
		$('#ubicacionEliminalbl').text( $(this).siblings("a").find(".numUbicacionEndo").text() );
		$( '#modalCerrarTab' ).modal( {
			show : true
		} );
	} else {
		showMessageError( '.navbar', "Error al guardar su información", 0 );
		hideLoader();
	}

} );

$( "#btnEliminarPestania" ).click( function(e) {
	showLoader();
	e.preventDefault();
	
	var liElimina = $("#tabs > ul li ").has("a .numUbicacionEndo:contains(" + $('#ubicacionEliminalbl').text() + ")");
	var ubica = $( liElimina ).attr( "aria-controls" );
	var sepUbica = ubica.split( "-" );
	
	infoCotJson.noUbicaciones -= 1;
	llenaObjUbicacion( tipoGuardar.ELIMINAR_UBICACION, parseInt(sepUbica[1]) );
	enviaUbicaciones();


} );


$("#btnRegresarPasoAnt").click(function (){
	regresaPaso1();
});

function regresaPaso1(){
	showLoader();
	actualizainfoCot();
	$.post( redirigeURL, {
		infoCot : JSON.stringify( infoCotJson ),
		paso : "/paso3"
	} ).done( function(data) {
		var response = JSON.parse( data );
		if (response.code == 0) {
			$("#infoCotizacionBack").val(response.msg);
			$("#paso2-form-back").submit();
		} else {
			showMessageError( '.navbar', response.msg, 0 );
			hideLoader();
		}
	} );
}

function seleccionaVentana(){
	return "/homeowner-quotation";
}

function actualizainfoCot(){
	switch (infoCotJson.modo) {
		case modo.NUEVA:
			if(perfilJapones == "1"){
				infoCotJson.modo = modo.EDICION_JAPONES;	
			}
			else{
				infoCotJson.modo = modo.EDICION;
			}
			break;
		case modo.COPIA:
			infoCotJson.modo = modo.EDICION;
			break;
		case modo.ALTA_ENDOSO:
			infoCotJson.modo = modo.EDITAR_ALTA_ENDOSO;
			break;
		case modo.BAJA_ENDOSO:
			infoCotJson.modo = modo.EDITAR_BAJA_ENDOSO;
			break;
		
		default:
			break;
	}
	
}


function validaRestriccionesSuscripcion(){
	validaSubgiroRiesgo();
	fExcedeLimites();
}

function validaSubgiroRiesgo(){
	if(subGiroRiesgo == '1'){
		if(perfilSuscriptor == '0'){
			$("#paso2_next").addClass("d-none");
			$("#infoPrimas").addClass("d-none");
			$("#btnsuscontinuar").removeClass("d-none");
		}
	}
}

function fExcedeLimites(){
	if(excedeLimites == '1'){
		if(aceptaSuscripcion == '1'){
			$("#paso2_next").addClass("d-none");
			$("#infoPrimas").addClass("d-none");
			$("#btnsuscontinuar").removeClass("d-none");
		}
		
	}
}

function aplicaReglasEdoCotizacion(){
	var infoP1Json = JSON.parse(infoP1);
	if(!valIsNullOrEmpty(infoP1Json.subEstado)){
		if(perfilSuscriptor != '1'){
			switch (infoP1Json.subEstado) {
				case edoCotizacion.COTIZADO_SUSCRIPTOR:
					ocultaCamposSubEdo();
					break;
				case edoCotizacion.RECHAZO_VBA_492:
					ocultaCamposSubEdo();
					break;
				default:
					break;
			}
		}else{
			
			if(infoCotJson.modo == modo.CONSULTA){
				ocultaCamposSubEdo();				
			}
		}
	}
}

function ocultaCamposSubEdo(){
	$("#contenPaso2 input, textarea, select ").attr("disabled", true);
	$("#chkBjaEnd .form-check-input, #comentariosDosSuscrip ").attr("disabled", false);
	$("#btn-add-tab-endosos").addClass("d-none");
	$("#save_tot2").addClass("d-none");
	$("#infoPrimas").addClass("d-none");
	$( "#tabs button.close" ).addClass( "d-none" );
}


$("#btnsuscontinuar").click(function(){
	$("#chkBjaEnd .form-check-input, #comentariosDosSuscrip ").attr("disabled", false);
	$('#fileModal').modal({
        show: true,
        backdrop: 'static',
        keyboard: false
    });
});

$('#fileModal #docAgenSusc').change(function(evt) {
    cargaDocumentos(evt, '#fileModal', '');
});

function cargaDocumentos(evt, padre, iddoc) {
    var listMimetypeValid = ["application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/pdf"
    ];
   
    var btn = $("#docAgenSusc");
    var maxSize = 5;
    var files = evt.target.files; /* FileList object */
    var nomInvalidos = "";
    const dt = new DataTransfer();
    $(padre + ' #infDocSuc' + iddoc).val('');
   /* $.each(files, function(key, file) {
        if (listMimetypeValid.indexOf(file.type) < 0) {
            nomInvalidos += (nomInvalidos == "") ? file.name : ", " + file.name;
        } else {
            dt.items.add(file);
            var nombres = $(padre + ' #infDocSuc' + iddoc).val();
            nombres += (nombres == '') ? file.name : ", " + file.name;
            $(padre + ' #infDocSuc' + iddoc).val(nombres);
        }
    });*/
    $.each(files, function(key, file) {

        var pesoArchivo = file.size / 1024 / 1024; /*peso en megas*/

        var agregarArchivo = true;
        if (listMimetypeValid.indexOf(file.type) < 0) {
            agregarArchivo = false;
            nomInvalidos += (nomInvalidos == "") ? file.name : ",<br>" + file.name;
            nomInvalidos += "- Tipo de archivo no admitido";
        }
        if (pesoArchivo > maxSize) {
            agregarArchivo = false;
            nomInvalidos += (nomInvalidos == "") ? file.name : ",<br>" + file.name;
            nomInvalidos += " - El archivo que quiere cargar pesa más de 5 MB, favor de reducir la resolución o cargar otro más ligero"
        }

        if (agregarArchivo) {
            dt.items.add(file);
            var nombres = $(padre + ' #infDocSuc' + iddoc).val();
            nombres += (nombres == '') ? file.name : ", " + file.name;
            $(padre + ' #infDocSuc' + iddoc).val(nombres);
            $(btn).parent().addClass('btn-green');
            $(btn).parent().removeClass('btn-blue');
            $(btn).parent().removeClass('btn-purple');
            $(btn).parent().removeClass('btn-red');
        } else {

            $(btn).parent().addClass('btn-purple');
            $(btn).parent().removeClass('btn-blue');
            $(btn).parent().removeClass('btn-green');
            $(btn).parent().removeClass('btn-red');
            $(btn).val('');
        }
    });
    $(padre + ' #docAgenSusc' + iddoc).files = dt.files;
    if (nomInvalidos != "") {
        showMessageError(padre + ' .modal-header', "Archivo(s) invalido(s): " + nomInvalidos, 0);

    }
}

$("#btnSuscripEnvSus").click(function(){
	showLoader();
	adjuntaArchivos();
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

/*    data.append('isRevire', $('#txtAuxEnvDoc').val()); */
    data.append('auxiliarDoc', auxiliarDoc);
    data.append('comentarios', $('#comentariosDosSuscrip').val());
    data.append('infoCot', JSON.stringify(infoCotJson));
    data.append('url', url.origin + url.pathname); 
    data.append('url2', url.origin);
    data.append('totArchivos', $('#docAgenSusc')[0].files.length);

    $.ajax({
        url: sendMailAgenteSuscriptorURL,
        data: data,
        processData: false,
        contentType: false,
        type: 'POST',
        success: function(data) {
            if (data != "") {
                var response = jQuery.parseJSON(data);
                if (response.code > 0) {
                    $('#fileModal').modal('hide');
                    hideLoader();
                    showMessageError('.navbar', response.msg, 0);
                } else {
                    window.location.href = url.origin + url.pathname.replace("/paso2", seleccionaVentana());
                    
   
                }
            } else {
            	showMessageError('.navbar', "Error al enviar la información", 0);
            }
        }
    });
}


$("#con_baja").click(function(){
	var tot_checked = 0;
	var ub = "";
	$.each( $("#chkBjaEnd .form-check :checkbox"), function(key, chek) {
		if($(chek).is(":checked")){
			tot_checked++;
			if(valIsNullOrEmpty(ub)){
				ub =  $(chek).siblings("label").text();
			}else{
				ub += ", " +  $(chek).siblings("label").text();
			}
		}
	} );
	if(tot_checked > 0){
		$("#modalBajaUb #spnUbElim").text(ub);
		$("#modalBajaUb").modal("show");
	}else{
		showMessageError( '.navbar', "Para continuar, seleccione al menos una ubicación", 0 );
	}
	
});

$("#btnAceptarBajaMdl").click(function(e){
	showLoader();
	/**/
	$.post( guardaBajaEndosoURL, {
		listaUbicaciones : $("#modalBajaUb #spnUbElim").text(),
		infCotString : JSON.stringify( infoCotJson ),
		infP1 : infoP1
	} ).done( function(data) {
		var response = JSON.parse( data );
		if(response.code > 0){
			$('#modalBajaUb').modal('hide');
			hideLoader();
			showMessageError( '.navbar', response.msg , 0 );
			
		}else{
			infoCotJson.folio = response.folio;
			infoCotJson.cotizacion = response.cotizacion;
			infoCotJson.version = response.version;
			redirigeP3();
		}
		
	});
});

$(".niveles").on('blur', function() {
	
	var auxNiveles = parseInt($(this).val());
	
	if(auxNiveles < 1 || auxNiveles > 50) {
		showMessageError( '.navbar', "Ingrese un numero entre 1 y 50", 0);
		$(this).val('');
		$(this).focus();
	}
});

$(".nivelRiesgo").on('blur', function(){
	
	var ubicacionId = $(this).closest('.ubicacion').attr('id');
	
	if(valIsNullOrEmpty($("#" + ubicacionId + " .niveles").val())) {
		showMessageError( '.navbar', "Primero capture los niveles", 0);
		$(this).val('');
		$("#" + ubicacionId + " .niveles").focus();
	}
	
	var auxNivelRiesgo = parseInt($(this).val());
	var auxNiveles = parseInt($("#" + ubicacionId + " .niveles").val());
	
	if((auxNivelRiesgo > auxNiveles) || (auxNivelRiesgo == 0)) {
		showMessageError( '.navbar', "El nivel del riesgo no puede ser mayor a los niveles o igual a 0", 0);
		$(this).val('');
		$(this).focus();
	}
	
});

$(".tipoTechos").on("change", function(){
	
	if($(this).val() == "6196" && (perfilSuscriptor != 1 && perfilJapones != 1)) {
		showMessageError( '.navbar', "La cotización será turnada a suscripción. RAZON: Techos de Madera", 0);
	}
});

$(".checkAsegurado").on("change", function(){
	var ubicacionId = $(this).closest('.ubicacion').attr('id');
	
	if($(this).is(":checked")){	
		agregaPersona(ubicacionId, 'aa');
	}
	else{
		var auxUbiId = parseInt(ubicacionId.split("-")[1]) - 1;
		$("#" + ubicacionId + " .rowAsegurado").remove();
		contadorAsegurados[auxUbiId] = 0;
	}
});

$(".checkBeneficiario").on("change", function(){
	var ubicacionId = $(this).closest('.ubicacion').attr('id');
	
	if($(this).is(":checked")){	
		agregaPersona(ubicacionId, 'bp');
	}
	else{
		var auxUbiId = parseInt(ubicacionId.split("-")[1]) - 1;
		$("#" + ubicacionId + " .rowBeneficiario").remove();
		contadorAsegurados[auxUbiId] = 0;
	}
});

function actualizaCampo(campo, contadorAsegVar) {
	var auxId = $(campo).attr('id');
	var auxLabel = $(campo).siblings('label');
	
	$(campo).attr('id', auxId + "-" + contadorAsegVar);
	$(auxLabel).attr('for', auxId + "-" + contadorAsegVar);
	
}

function actualizaCampoRadio(campo, contadorAsegVar, ubicacionIdRadio) {
	var auxId = $(campo).attr('id');
	var auxLabel = $(campo).siblings('label');
	
	$(campo).attr('id', auxId + "-" + ubicacionIdRadio + "-"+ contadorAsegVar);
	$(auxLabel).attr('for', auxId + "-" + ubicacionIdRadio + "-"+ contadorAsegVar);
	
}

function actualizaClase(clase, contadorAsegVar) {
	
	$.each($(clase), function(key, value) {
		
		var auxClase = clase.substring(1);
		
		$(this).removeClass(auxClase);
		$(this).addClass(auxClase + "-" + contadorAsegVar);
	});
}

$( '.dynamicAsegurado' ).on("click", '.eliminaAseg', function() {	
	
	var ubicacionId = $(this).closest('.ubicacion').attr('id');
	var auxString = ubicacionId.split("-");
	var auxContador = parseInt(auxString[1]) - 1;
	
	contadorAsegurados[auxContador]--;
	
	if(contadorAsegurados[auxContador] == 0){
		$("#" + ubicacionId + " .checkAsegurado").removeAttr('checked');
	}
	
	$(this).closest('.rowAsegurado').remove();
});

$( '.dynamicBeneficiario' ).on("click", '.eliminaBenef', function() {	
	
	var ubicacionId = $(this).closest('.ubicacion').attr('id');
	var auxString = ubicacionId.split("-");
	var auxContador = parseInt(auxString[1]) - 1;
	
	contadorBeneficiarios[auxContador]--;
	
	if(contadorBeneficiarios[auxContador] == 0){
		$("#" + ubicacionId + " .checkBeneficiario").removeAttr('checked');
	}
	
	$(this).closest('.rowBeneficiario').remove();
});

$( '.dynamicAsegurado, .dynamicBeneficiario' ).on("click", '.aplicaUbicaciones', function() {	
	
	var auxTipo = $(this).closest('.rowAsegurado, .rowBeneficiario').parent().attr('class');
	var auxIdRow = $(this).closest('.rowAsegurado, .rowBeneficiario').attr('id');
	var auxIdUbicacion = $(this).closest('.ubicacion').attr('id');
	var auxPersonasTemporales;
	var auxTipoRow = "";
	var auxPrefijo = "";
	var auxElimina = "";
	var auxContador;
	
	switch(auxTipo) {
		case 'dynamicAsegurado':
			auxTipoRow = "rowAsegurado";
			auxPrefijo = "aa";
			auxPersonasTemporales = generaAsegurados(0);
			auxElimina = "eliminaAseg";
			auxContador = contadorAsegurados;
			break;
		case 'dynamicBeneficiario':
			auxTipoRow = "rowBeneficiario";
			auxPrefijo = "bp";
			auxPersonasTemporales = generaBeneficiarios(0);
			auxElimina = "eliminaBenef";
			auxContador = contadorBeneficiarios;
			break;
	}
	
	console.log(auxPersonasTemporales);
	
	var auxRfc = $("#" + auxIdUbicacion + " #" + auxIdRow + " .rfc").val();
	var auxIdPersona = $("#" + auxIdUbicacion + " #" + auxIdRow + " .rfc").attr('idPersona');
	
	var auxSeleccionado = $(this).is(':checked');
	
	$.each(auxPersonasTemporales, function(key, value){
		
		if(value.idPersona == auxIdPersona){
		
			$.each($('.ubicacion'), function(keyU, valueU){
				if(keyU != 0){
					
					var auxCoincidencias = 0;
					
					if(auxSeleccionado){
						$.each($("#ubicacion-" + (keyU + 1) + " ."+auxTipoRow), function(keyR, valueR){
							
							if($(valueR).find('.rfc').attr('idPersona') == value.idPersona){
								auxCoincidencias++;
							}
						});
						
						
						if(auxCoincidencias == 0){
							if(auxContador[keyU] < 3) {
								agregaPersonaLleno("ubicacion-" + (keyU + 1), value, auxPrefijo);
							}
							else {
								
							}
						}
					}
					else {
						$.each($("#ubicacion-" + (keyU + 1) + " ."+auxTipoRow), function(keyR, valueR){
							
							if($(valueR).find('.rfc').attr('idPersona') == value.idPersona){
								$(valueR).find('.' + auxElimina).click();
							}
						});
					}
				}
			});
		}
	});
	
});

function agregaPersona(ubicacionId, tipo){
	
	var tipoAdicional;
	var claseAdicional;
	var personaAdicionalURL;
	var auxContadorPersonas;
	
	switch(tipo){
		case 'aa':
			tipoAdicional = 'Asegurado';
			claseAdicional = 'agregaAseg';
			personaAdicionalURL = aseguradoAdicionalURL;
			auxContadorPersonas = contadorAsegurados;
			auxConsecutivoPersonas = idConsecutivoAseg;
			break;
		case 'bp':
			tipoAdicional = 'Beneficiario';
			claseAdicional = 'agregaBenef';
			personaAdicionalURL = beneficiarioPreferenteURL;
			auxContadorPersonas = contadorBeneficiarios;
			auxConsecutivoPersonas = idConsecutivoBenef;
			break;
		default:
			tipoAdicional = 'Error';
			break;
	}
	
	var auxString = ubicacionId.split("-");
	var auxContador = parseInt(auxString[1]) - 1;
	
	if(auxContadorPersonas[auxContador] < 3){
		
		$("#" + ubicacionId +" .dynamic" + tipoAdicional).append(`<div class='row`+ tipoAdicional + `' id='row` + tipoAdicional + auxConsecutivoPersonas[auxContador] + `'>
															</div>`);
		
		(function(indexAux){
			$("#" + ubicacionId + " #row" + tipoAdicional + indexAux).load(personaAdicionalURL, function(){
				
				var auxId = $("#" + tipo + "_denominacion").attr('id');
				
				$("#" + tipo + "_denominacion").attr('id', auxId + "-" + indexAux);
				$("#" + ubicacionId + " #" + tipo + "_denominacion-" + indexAux).materialSelect();
				
				$("#radio_pm_" + tipo).attr('name', 'group' + tipoAdicional + '-' + ubicacionId + '-' + indexAux);
				$("#radio_pf_" + tipo).attr('name', 'group' + tipoAdicional + '-' + ubicacionId + '-' + indexAux);
				
				
				actualizaCampoRadio("#radio_pm_" + tipo, indexAux, ubicacionId);
				actualizaCampoRadio("#radio_pf_" + tipo, indexAux, ubicacionId);
				actualizaCampo("#" + tipo + "_rfc", indexAux);
				actualizaCampo("#" + tipo + "_nombrecontratante", indexAux);
				actualizaCampo("#" + tipo + "_fisnombre", indexAux);
				actualizaCampo("#" + tipo + "_fispaterno", indexAux);
				actualizaCampo("#" + tipo + "_fismaterno", indexAux);
				actualizaCampo("#" + tipo + "_aplicaUbicaciones", indexAux);
				
				actualizaClase(".tip_fisica_" + tipo, indexAux);
				actualizaClase(".tip_moral_" + tipo, indexAux);
				actualizaClase("." + claseAdicional, indexAux);
				
				$("#" + ubicacionId + " ." + claseAdicional + "-" + indexAux).attr('onclick', "agregaPersona('" + ubicacionId + "', '" + tipo + "')");
				
				if(ubicacionId.split("-")[1] != "1"){
					$("#" + ubicacionId + " #row" + tipoAdicional + indexAux + " #" + tipo + "_aplicaUbicaciones-" + indexAux).parent().addClass('d-none');
				}
			});
		})(auxConsecutivoPersonas[auxContador]);
		
		switch(tipo){
			case 'aa':
				contadorAsegurados[auxContador]++;
				idConsecutivoAseg[auxContador]++;
				break;
			case 'bp':
				contadorBeneficiarios[auxContador]++;
				idConsecutivoBenef[auxContador]++;
				break;
		}
		
		
	}
	else {
		showMessageError('.navbar', "Solo se puede agregar un máximo de 3 Asegurados Adicionales y Beneficiarios Preferentes", 0);
	}
}

function agregaPersonaLleno(ubicacionId, infoPersonaAdicional, tipo){
	
	console.log(infoPersonaAdicional);
	
	var tipoAdicional;
	var claseAdicional;
	var personaAdicionalURL;
	var auxContadorPersonas;
	var auxElimina;
	
	switch(tipo){
		case 'aa':
			tipoAdicional = 'Asegurado';
			claseAdicional = 'agregaAseg';
			personaAdicionalURL = aseguradoAdicionalURL;
			auxContadorPersonas = contadorAsegurados;
			auxConsecutivoPersonas = idConsecutivoAseg;
			auxElimina = 'eliminaAseg';
			break;
		case 'bp':
			tipoAdicional = 'Beneficiario';
			claseAdicional = 'agregaBenef';
			personaAdicionalURL = beneficiarioPreferenteURL;
			auxContadorPersonas = contadorBeneficiarios;
			auxConsecutivoPersonas = idConsecutivoBenef;
			auxElimina = 'eliminaBenef';
			break;
		default:
			tipoAdicional = 'Error';
			break;
	}
	
	var auxString = ubicacionId.split("-");
	var auxContador = parseInt(auxString[1]) - 1;
	
	if(auxContadorPersonas[auxContador] < 3){
		
		$("#" + ubicacionId +" .dynamic" + tipoAdicional).append(`<div class='row`+ tipoAdicional + `' id='row` + tipoAdicional + auxConsecutivoPersonas[auxContador] + `'>
															</div>`);
		
		(function(indexAux){
			$.ajax({
				async: false,
				type: 'GET',
				url: personaAdicionalURL, 
				success: function(data){
				
				$("#" + ubicacionId + " #row" + tipoAdicional + indexAux).append(data)
				
				var auxId = $("#" + tipo + "_denominacion").attr('id');
				
				$("#" + tipo + "_denominacion").attr('id', auxId + "-" + indexAux);
				//$("#" + ubicacionId + " #" + tipo + "_denominacion-" + indexAux).materialSelect();
				
				$("#radio_pm_" + tipo).attr('name', 'group' + tipoAdicional + '-' + ubicacionId + '-' + indexAux);
				$("#radio_pf_" + tipo).attr('name', 'group' + tipoAdicional + '-' + ubicacionId + '-' + indexAux);
				
				
				actualizaCampoRadio("#radio_pm_" + tipo, indexAux, ubicacionId);
				actualizaCampoRadio("#radio_pf_" + tipo, indexAux, ubicacionId);
				actualizaCampo("#" + tipo + "_rfc", indexAux);
				actualizaCampo("#" + tipo + "_nombrecontratante", indexAux);
				actualizaCampo("#" + tipo + "_fisnombre", indexAux);
				actualizaCampo("#" + tipo + "_fispaterno", indexAux);
				actualizaCampo("#" + tipo + "_fismaterno", indexAux);
				actualizaCampo("#" + tipo + "_aplicaUbicaciones", indexAux);
				actualizaCampo("#" + tipo + "_chktoggle", indexAux);
				
				actualizaClase(".tip_fisica_" + tipo, indexAux);
				actualizaClase(".tip_moral_" + tipo, indexAux);
				actualizaClase("." + claseAdicional, indexAux);
				
				$("#" + ubicacionId + " ." + claseAdicional + "-" + indexAux).attr('onclick', "agregaPersona('" + ubicacionId + "', '" + tipo + "')");
				
				$("#" + ubicacionId + " #row" + tipoAdicional + indexAux + " #" + tipo + "_rfc-" + indexAux).val(infoPersonaAdicional.rfc);
				$("#" + ubicacionId + " #row" + tipoAdicional + indexAux + " #" + tipo + "_rfc-" + indexAux).siblings('label').addClass('active');
				
				$("#" + ubicacionId + " #row" + tipoAdicional + indexAux + " #" + tipo + "_rfc-" + indexAux).attr('codigo', infoPersonaAdicional.codigo);
				$("#" + ubicacionId + " #row" + tipoAdicional + indexAux + " #" + tipo + "_rfc-" + indexAux).attr('idPersona', infoPersonaAdicional.idPersona);
				
				switch(infoPersonaAdicional.tipoPer) { 
					case 217:
						
						$("#radio_pf_" + tipo + "-" + ubicacionId + "-" + indexAux).click();
				
						$("#" + ubicacionId + " #row" + tipoAdicional + indexAux + " #" + tipo + "_fisnombre-" + indexAux).val(infoPersonaAdicional.nombre);
						$("#" + ubicacionId + " #row" + tipoAdicional + indexAux + " #" + tipo + "_fisnombre-" + indexAux).siblings('label').addClass('active');
						
						$("#" + ubicacionId + " #row" + tipoAdicional + indexAux + " #" + tipo + "_fispaterno-" + indexAux).val(infoPersonaAdicional.appPaterno);
						$("#" + ubicacionId + " #row" + tipoAdicional + indexAux + " #" + tipo + "_fispaterno-" + indexAux).siblings('label').addClass('active');
				
						$("#" + ubicacionId + " #row" + tipoAdicional + indexAux + " #" + tipo + "_fismaterno-" + indexAux).val(infoPersonaAdicional.appMaterno);
						$("#" + ubicacionId + " #row" + tipoAdicional + indexAux + " #" + tipo + "_fismaterno-" + indexAux).siblings('label').addClass('active');
						
						if(infoPersonaAdicional.extranjero == 1) {
							$("#" + ubicacionId + " #row" + tipoAdicional + indexAux + " #" + tipo + "_chktoggle-" + indexAux).attr('checked', true);
						}
						
						break;
					case 218:
						$("#radio_pm_" + tipo + "-" + ubicacionId + "-" + indexAux).click();
						
						$("#" + ubicacionId + " #row" + tipoAdicional + indexAux + " #" + tipo + "_nombrecontratante-" + indexAux).val(infoPersonaAdicional.nombre);
						$("#" + ubicacionId + " #row" + tipoAdicional + indexAux + " #" + tipo + "_nombrecontratante-" + indexAux).siblings('label').addClass('active');
						
						$("#" + ubicacionId + " #row" + tipoAdicional + indexAux + " #" + tipo + "_denominacion-" + indexAux).val(infoPersonaAdicional.idDenominacion);
						//$("#" + ubicacionId + " #row" + tipoAdicional + indexAux + " #" + tipo + "_denominacion-" + indexAux).materialSelect();
						
						break;
				}
				
				
				if(infoPersonaAdicional.aplicaUbicaciones) {
					/*
					$("#" + ubicacionId + " #row" + tipoAdicional + indexAux + " #" + tipo + "_aplicaUbicaciones-" + indexAux).attr('checked', true);
					*/
					$("#" + ubicacionId + " #row" + tipoAdicional + indexAux + " #" + tipo + "_aplicaUbicaciones-" + indexAux).click();
				}
				
				
				if(ubicacionId.split("-")[1] != "1"){
					$("#" + ubicacionId + " #row" + tipoAdicional + indexAux + " #" + tipo + "_aplicaUbicaciones-" + indexAux).parent().addClass('d-none');
					if(infoPersonaAdicional.aplicaUbicaciones) {
						$("#" + ubicacionId + " #row" + tipoAdicional + indexAux + " ." + auxElimina).addClass('d-none');
						$("#" + ubicacionId + " #row" + tipoAdicional + indexAux + " input").attr('disabled', true);
					}
				}
			}
			}).done(function() {
				console.log("Termine la carga");
			});
		})(auxConsecutivoPersonas[auxContador]);
		
		switch(tipo){
			case 'aa':
				contadorAsegurados[auxContador]++;
				idConsecutivoAseg[auxContador]++;
				break;
			case 'bp':
				contadorBeneficiarios[auxContador]++;
				idConsecutivoBenef[auxContador]++;
				break;
		}
		
		
	}
	else {
		showMessageError('.navbar', "Solo se puede agregar un máximo de 3 Asegurados Adicionales y Beneficiarios Preferentes", 0);
	}
}