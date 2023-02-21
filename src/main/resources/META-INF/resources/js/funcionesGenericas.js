/**
 * Agrega clase al campo espesifico para ocultarlo
 * mandar el selector tipo jquery
 * id -> #id
 * class -> .class
 * @param selector
 * @returns
 */
function ocultaCampos(selector){
	$(selector).addClass("d-none");
	$(selector).removeClass("d-block");
}

/**
 * Agrega clase al campo espesifico para mostrarlo
 * mandar el selector tipo jquery
 * id -> #id
 * class -> .class
 * @param selector
 * @returns
 */
function muestraCampos(selector){
	$(selector).addClass("d-block");
	$(selector).removeClass("d-none");
}

function activaCampos(campo){
	if(valIsNullOrEmpty($(campo).val())){
		$(campo).siblings('label').removeClass('active');
	}else{
		$(campo).siblings('label').addClass('active');
	}
}

/**
 * Destrulle y regenera los material select
 * @param objeto
 * @param enabled
 * @returns
 */
function selectDestroy(objeto, enabled) {
    $(objeto).prop("disabled", enabled);
    $(objeto).materialSelect('destroy');
    $(objeto).materialSelect();
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

function vaciosInpText(value) {	
	var errores = false;	
	if($(value).is(":visible")){
		if (valIsNullOrEmpty($(value).val())) {
			errores = true;
			$(value).addClass('invalid');
			$(value).parent().append(
					"<div class=\"alert alert-danger\" role=\"alert\"> <span class=\"glyphicon glyphicon-ban-circle\"></span>" + " "
					+ msj.es.campoRequerido + "</div>");
		}			
	}
	return errores;
}


$("#contPaso1 :input").change(function(){
	removeClassInvalid();
});

$("#contPaso1 :input").click(function(){
	removeClassInvalid();
});

$("#cotizadores-p2 :input").change(function(){
	removeClassInvalid();
});

$("#cotizadores-p2 :input").click(function(){
	removeClassInvalid();
});

function removeClassInvalid(){
	$(".alert-danger").remove();
    $('.invalid').removeClass('invalid');
}

/**
 * llena input tex 
 * @param campo
 * @param valor
 * @param disabled
 * @returns
 */
function llenaCampoText(campo, valor, disabled){
	$(campo).val(valor);
	activaCampos(campo)
	$(campo).prop("disabled", disabled);
}


function valIsNullOrEmpty(value) {
	if (value === undefined) {
		return true;
	}
	value = value.trim();
	return (value == null || value == "null" || value === "");
}


function deshabilitaRadio(selector, disabled){
	$(selector).find(".form-check-input").prop("disabled", disabled);
}

function seleccionaOpcionSelect(campo, value, disabled){
	$(campo + " option[value = '"+ value +"' ]").attr("selected", true)
	selectDestroy(campo, disabled);
}

function daFormatoMoneda(campo){
	if(!valIsNullOrEmpty($( campo ).val())){
		$( campo ).val(formatter.format($( campo ).val()));
	}
}


$(".acordeon .moneda" ).on("blur", function(){
	this.value = this.value.replace(/[^0-9\.]/g,'');
	daFormatoMoneda($(this));
});

$(".acordeon .moneda" ).on("keyup", function(event){
	var aux = $(event.target).val().split('.');
	$(event.target).val(aux[0]);
	 $(event.target).val(function (index, value ) {
	        return value.replace(/\D/g, "").replace(/\B(?=(\d{3})+(?!\d)\.?)/g, ",");
	    });
});

$(".acordeon .moneda" ).on("focus", function(){
	/*this.value = this.value.replace(/[^0-9\.,]/g,'');*/
	var abc = $(this).val().replace(/[^0-9\.,]/g,'');
	$(this).val(abc.split('.')[0]);
});

$("#tabs .cpValido" ).on("keyup", function(){
	this.value = this.value.replace(/[^0-9\.]/g,'');
});

$("#tabs .longlat" ).on("keyup", function(){
	var expRe  = /^(-)?$/;
	var expRe2 = /^(-)?(\d{1,3})$/ ;
	var expRe3 = /^(-)?(\d{1,3})(\.)$/ ;
	var expRe4 = /^(-)?(\d{1,3})(\.)(\d{1,6})$/ ;
	var valido = expRe.test(this.value) || expRe2.test(this.value) || expRe3.test(this.value) || expRe4.test(this.value);
	if(!valido){
		this.value = this.value.slice(0, -1);
	}
});

$("#tabs .longlat" ).on("keypress", function(){
	var expRe  = /^(-)?$/;
	var expRe2 = /^(-)?(\d{1,3})$/ ;
	var expRe3 = /^(-)?(\d{1,3})(\.)$/ ;
	var expRe4 = /^(-)?(\d{1,3})(\.)(\d{1,6})$/ ;
	var valido = expRe.test(this.value) || expRe2.test(this.value) || expRe3.test(this.value) || expRe4.test(this.value);
	if(!valido){
		this.value = this.value.slice(0, -1);
	}
});


/*
$(".infReq").focus(function(){
	$(".alert-danger").remove();
    $('.invalid').removeClass('invalid');
});

$(".infReq").change(function(){
	$(".alert-danger").remove();
    $('.invalid').removeClass('invalid');
});
*/


function selectDestroy(objeto, enabled) {
    $(objeto).prop("disabled", enabled);
    $(objeto).materialSelect('destroy');
    $(objeto).materialSelect();
}

function validaFormatoRfc(rfc, aceptarGenerico = true) {
	const re       = /^([A-ZÑ&]{3,4}) ?(?:- ?)?(\d{2}(?:0[1-9]|1[0-2])(?:0[1-9]|[12]\d|3[01])) ?(?:- ?)?([A-Z\d]{2})([A\d])$/;
    var   validado = rfc.match(re);

    if (!validado)  //Coincide con el formato general del regex?
        return false;

    //Separar el dígito verificador del resto del RFC
    const digitoVerificador = validado.pop(),
          rfcSinDigito      = validado.slice(1).join(''),
          len               = rfcSinDigito.length,

    //Obtener el digito esperado
          diccionario       = "0123456789ABCDEFGHIJKLMN&OPQRSTUVWXYZ Ñ",
          indice            = len + 1;
    var   suma,
          digitoEsperado;

    if (len == 12) suma = 0
    else suma = 481; //Ajuste para persona moral

    for(var i=0; i<len; i++)
        suma += diccionario.indexOf(rfcSinDigito.charAt(i)) * (indice - i);
    digitoEsperado = 11 - suma % 11;
    if (digitoEsperado == 11) digitoEsperado = 0;
    else if (digitoEsperado == 10) digitoEsperado = "A";

    //El dígito verificador coincide con el esperado?
    // o es un RFC Genérico (ventas a público general)?
    if ((digitoVerificador != digitoEsperado)
     && (!aceptarGenerico || rfcSinDigito + digitoVerificador != "XAXX010101000"))
        return false;
    else if (!aceptarGenerico && rfcSinDigito + digitoVerificador == "XEXX010101000")
        return false;
    return rfcSinDigito + digitoVerificador;
}