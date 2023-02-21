/**
 * Modo para el flujo de cotizaciones
 */
const modo = {
    NUEVA: "NUEVA",
    EDICION: "EDICION",
    COPIA: "COPIA",
    AUX_PASO4: "AUX_PASO4",
    ALTA_ENDOSO: "ALTA_ENDOSO",
    BAJA_ENDOSO: "BAJA_ENDOSO",
    EDITAR_ALTA_ENDOSO : "EDITAR_ALTA_ENDOSO",
    EDITAR_BAJA_ENDOSO : "EDITAR_BAJA_ENDOSO",
    CONSULTA : "CONSULTA",
    FACTURA_492 : "FACTURA_492",
	ERROR : "ERROR",
    RENOVACION_AUTOMATICA : "RENOVACION_AUTOMATICA",
    CONSULTAR_RENOVACION_AUTOMATICA : "CONSULTAR_RENOVACION_AUTOMATICA",
	EDICION_JAPONES : "EDICION_JAPONES",
	CONSULTAR_REVISION: "CONSULTAR_REVISION"
};


const tipoCotizacion = {
	ERROR : "ERROR",
	FAMILIAR : "FAMILIAR",
	EMPRESARIAL : "EMPRESARIAL"
};

const tipoPersona = {
		FISICA : "FISICA",
		MORAL : "MORAL"
};

const formatter = new Intl.NumberFormat('en-US', {
	  style: 'currency',
	  currency: 'USD',
	  minimumFractionDigits: 2
});

const msj = {
		es : {
			errorInformacion : "Error al  cargar la información",
			catSinInfo: "Catalogo sin información",
	        campoRequerido: "El campo es requerido",
	        faltaInfo: "Hace falta información requerida",
	        errorGuardar: "Error al guardar su información"
		}
};

/**
 * objeto para Url de Resources Command 
 */
var ligasServicios = {
	listaPersonas : "",
	listaSubgiros : "",
	guardaInfo : "",
	redirige : ""
};

/**
 * rfc genericos que se descartan
 */
var rfcGenerico = ["XAXX010101000", "XEXX010101000"];

/**
 * Variables globales auxiliares j
 */
var auxP1 = {
	infoClientExistenttEncontrado : null,
	auxPrefijo : null,
	auxIdUbi : null,
	auxId : null
};

/**
 * informacion necesaria para guardar
 */
var DatosGenerales = {
	
	tipomov :  0,
	vigencia :  0,
	fecinicio :  "",
	fecfin :  "",
	moneda :  0,
	formapago :  0,
	agente :  0,
	idPersona :  0,
	tipoPer :  0,
	rfc : ""  ,
	nombre :  "",
	appPaterno :  "",
	appMaterno :  "",
	idDenominacion :  0,
	codigo :  "",
	canalNegocio : 0,
	coaseguro: 0,
	modo :  "",
	tipoCot : "",
	cotizacion :  "",
	version :  0,
	giro :  0,
	subGiro :  0,
	noUbicaciones: 0,
	folio :  "",
	detalleSubGiro :  "",
	pantalla : "",
	p_permisoSubgiro :  0,
	subEstado : ""
};


var datosPaso1_1;

//Objetos Paso 2

const tipoGuardar = {
		GUARDAR_UBICACION : 1,
		AGREGAR_UBICACION : 2,
		ELIMINAR_UBICACION : 3
};

const NoEsELiminar = -1;

const edoCotizacion = {
		 SUBGIRO_RIESGO : "XS",
		 EXEDE_LIMITES : "XEL",
		 COTIZADO_SUSCRIPTOR : "CPS",
		 REVIRE_SUSCRIPTOR : "RAS",
		 TURNADO_A_492 : "T492",
		 VBA_492 : "VB492",
		 RECHAZO_VBA_492 : "RVB492"
};

const maxUbicaciones = {
		familiar : 5,
		empresarial : 20
};

/* es el objeto donde se setea la informacion de la cotizacion */
var infoCotJson =  null;

/* Es el objeto donde se setea la informacion de las ubicaciones */
var listaUbicaciones = new Object();

var continuar = false;

var idPestaniaElimina = -1;

var infoUltimaTab = {
		objeto : "",
		numero : "",
		etiqueta : ""
};

const enlace = {
	FAMILIAR : "/paquete-familiar",
	EMPRESARIAL : "/paquete-empresarial",
	PASO2 : "/paso2",
	PASO3 : "/paso3",
	PASO4 : "/paso4"
};