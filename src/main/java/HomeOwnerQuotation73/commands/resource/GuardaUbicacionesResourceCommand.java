package HomeOwnerQuotation73.commands.resource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.pa.cotizadorModularServices.Bean.InfoCotizacion;
import com.tokio.pa.cotizadorModularServices.Bean.UbicacionesResponse;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorPaso2;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorPaso2Familiar;
import com.tokio.pa.cotizadorModularServices.Util.CotizadorModularUtil;

import java.io.IOException;
import java.io.PrintWriter;

import javax.portlet.PortletSession;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import HomeOwnerQuotation73.constants.HomeOwnerQuotation73PortletKeys;

@Component(immediate = true, property = {
		"javax.portlet.name=" + HomeOwnerQuotation73PortletKeys.HOMEOWNERQUOTATION73,
		"mvc.command.name=/cotizadores/guardaubicaciones", }, service = MVCResourceCommand.class)

public class GuardaUbicacionesResourceCommand extends BaseMVCResourceCommand {

	@Reference
	CotizadorPaso2 _CMServicesP2;
	
	@Reference
	CotizadorPaso2Familiar _CMServicesP2Fam;

	@Override
	protected void doServeResource(ResourceRequest resourceRequest,
			ResourceResponse resourceResponse) throws Exception {
		// TODO Auto-generated method stub

		Gson gson = new Gson();

		String listaUbicaciones = ParamUtil.getString(resourceRequest, "listaUbicaciones");
		String infoCotizacionString = ParamUtil.getString(resourceRequest, "infCotString");

		InfoCotizacion infCotizacion = gson.fromJson(infoCotizacionString, InfoCotizacion.class);
		JsonObject objLisUbi = gson.fromJson(listaUbicaciones, JsonObject.class);

		guardaUbicaciones(resourceRequest, infCotizacion, objLisUbi, resourceResponse);

		resourceRequest.setAttribute("infoCotizacionString", infoCotizacionString);

	}

	private void guardaUbicaciones(ResourceRequest resourceRequest, InfoCotizacion infCotizacion,
			JsonObject objLisUbi, ResourceResponse resourceResponse) {

		HttpServletRequest originalRequest = PortalUtil
				.getOriginalServletRequest(PortalUtil.getHttpServletRequest(resourceRequest));
		User user = (User) resourceRequest.getAttribute(WebKeys.USER);
		int idPerfilUser = (int) originalRequest.getSession().getAttribute("idPerfil");

		objLisUbi.addProperty("p_usuario", user.getScreenName());
		objLisUbi.addProperty("idPerfil", idPerfilUser);
		objLisUbi.addProperty("p_pantalla", infCotizacion.getPantalla());

		try {
				guardaFamiliar(resourceRequest, objLisUbi, resourceResponse);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void guardaFamiliar(ResourceRequest resourceRequest, JsonObject objLisUbi,
			ResourceResponse resourceResponse) throws IOException {
		JsonObject respuesta = new JsonObject();
		PrintWriter writer = resourceResponse.getWriter();
		
		Gson gson = new Gson();
		
		try {

			System.out.println("---------------->aqui");
			
			UbicacionesResponse ubicacionResponse = _CMServicesP2Fam
					.guardaUbicacionFamiliarMejoras(objLisUbi);
			
			//UbicacionesResponse ubicacionResponse  = gson.fromJson("{\"code\":0,\"msg\":\"PROCESO TERMINADO CON EXITO\",\"cotizacion\":6271,\"folio\":\"112165\",\"version\":1,\"dataProviders\":[],\"primaTotal\":99.99,\"ubicaciones\":[{\"idubicacion\":1,\"calle\":\"Calle 5\",\"numero\":\"10\",\"cpData\":{\"idCp\":0,\"cp\":\"11290\",\"colonia\":\"Tehuantepec\",\"delegacion\":\"Miguel Hidalgo\",\"estado\":\"CDMX\",\"pais\":\"México\"},\"tipoInmueble\":0,\"tipoUso\":0,\"niveles\":0,\"descripcionConstruccion\":\"Casa\",\"descripcionMedidaSeguridad\":\"Medidas de Seguridad Casa\",\"valorInmueble\":999999,\"primaNeta\":15000,\"aseguradoAdicional\":[{\"idPersona\":100,\"tipoPer\":217,\"extranjero\":1,\"rfc\":\"BEBI771212HCC\",\"nombreCompleto\":\"Ivan Benitez Hernández\",\"nombre\":\"Ivan\",\"appPaterno\":\"Benitez\",\"appMaterno\":\"Hernández\",\"idDenominacion\":null,\"codigo\":null},{\"idPersona\":200,\"tipoPer\":217,\"extranjero\":0,\"rfc\":\"FLDUBI991110JNN\",\"nombreCompleto\":\"Uriel Flores Díaz\",\"nombre\":\"Uriel\",\"appPaterno\":\"Flores\",\"appMaterno\":\"Díaz\",\"idDenominacion\":null,\"codigo\":null}],\"fields\":[{\"type\":\"label\",\"predefinedValue\":null,\"name\":\"PFTITULOGEN\",\"label\":\"SUMA ASEGURADA POR COBERTURA\",\"required\":false,\"readOnly\":false,\"showLabel\":true,\"visibilityExpression\":\"\",\"tip\":null,\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"\"},{\"type\":\"checkbox\",\"predefinedValue\":null,\"name\":\"PFCHKBOXIKE\",\"label\":\"Asistencia en el Hogar IKE\",\"required\":true,\"readOnly\":false,\"showLabel\":true,\"visibilityExpression\":\"\",\"tip\":null,\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPEDIFICIO\",\"label\":\"Edificio\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":\"\",\"tip\":\"Suma Asegurada de Edificio\",\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"moneda\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPCONTENIDOS\",\"label\":\"Contenidos\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":\"\",\"tip\":\"Suma Asegurada de Contenidos\",\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"moneda\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPPERDIDAR\",\"label\":\"Pérdida de Rentas\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":\"\",\"tip\":\"Suma Asegurada de Pérdida de Rentas\",\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"moneda\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPGESTRA\",\"label\":\"Gastos Extraordinarios\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":\"\",\"tip\":\"Suma Asegurada de Gastos Extraordinarios\",\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"moneda\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPEEM\",\"label\":\"Equipo Electrónico Móvil\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":\"\",\"tip\":\"Suma Asegurada de Equipo Electrónico Móvil\",\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"moneda\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPRBSECI\",\"label\":\"Robo (Sección I)\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":\"\",\"tip\":\"Suma Asegurada de Robo (Sección I)\",\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"moneda\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPRBSECII\",\"label\":\"Robo (Sección II)\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":\"\",\"tip\":\"Suma Asegurada de Robo (Sección II)\",\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"moneda\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPRBSECIII\",\"label\":\"Robo (Sección III)\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":\"\",\"tip\":\"Suma Asegurada de Robo (Sección III)\",\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"moneda\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPDINVAL\",\"label\":\"Dinero y Valores\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":\"\",\"tip\":\"Suma Asegurada de Dinero y Valores\",\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"moneda\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPRC\",\"label\":\"Responsabilidad Civil\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":\"\",\"tip\":\"Suma Asegurada de Responsabilidad Civil\",\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"moneda\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPEEC\",\"label\":\"Equipo Electrónico\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":\"\",\"tip\":\"Suma Asegurada de Equipo Electrónico\",\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"moneda\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPCIRSTALES\",\"label\":\"Rotura de Cristales\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":\"\",\"tip\":\"Suma Asegurada de Rotura de Cristales\",\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"moneda\"},{\"type\":\"checkbox\",\"predefinedValue\":\"false\",\"name\":\"PFCHKBOXROTURADENTRO\",\"label\":\"¿Ampara rotura - robo de palos de golf dentro de la ubicación?\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":null,\"tip\":null,\"dataProviderName\":null,\"multiple\":null,\"options\":null,\"min\":null,\"max\":null,\"minlength\":null,\"maxlength\":null,\"pattern\":null,\"clase\":\"suscriptor\"},{\"type\":\"checkbox\",\"predefinedValue\":\"false\",\"name\":\"PFCHKBOXROTURAFUERA\",\"label\":\"¿Ampara rotura - robo de palos de golf fuera de la ubicación?\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":null,\"tip\":null,\"dataProviderName\":null,\"multiple\":null,\"options\":null,\"min\":null,\"max\":null,\"minlength\":null,\"maxlength\":null,\"pattern\":null,\"clase\":\"suscriptor\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPPALOSGOLF\",\"label\":\"Palos de golf\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":null,\"tip\":\"Suma Asegurada de Palos de Golf\",\"dataProviderName\":null,\"multiple\":null,\"options\":null,\"min\":null,\"max\":null,\"minlength\":null,\"maxlength\":null,\"pattern\":null,\"clase\":\"moneda suscriptor\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPSUPERGOLF\",\"label\":\"Super Golf\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":null,\"tip\":\"Suma Asegurada de Super Golf\",\"dataProviderName\":null,\"multiple\":null,\"options\":null,\"min\":null,\"max\":null,\"minlength\":null,\"maxlength\":null,\"pattern\":null,\"clase\":\"moneda suscriptor\"}],\"layout\":{\"rows\":[{\"columns\":[{\"size\":12,\"header\":\"Suma Asegurada por Cobertura\",\"accordion\":null,\"rows\":[{\"columns\":[{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPEDIFICIO\"]},{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPCONTENIDOS\"]},{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPPERDIDAR\"]}]},{\"columns\":[{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPGESTRA\"]},{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPCIRSTALES\"]},{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPRBSECI\"]}]},{\"columns\":[{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPRBSECII\"]},{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPRBSECIII\"]},{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPDINVAL\"]}]},{\"columns\":[{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPRC\"]},{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPEEC\"]},{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPEEM\"]}]},{\"columns\":[{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPPALOSGOLF\"]},{\"size\":8,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCHKBOXROTURADENTRO\"]}]},{\"columns\":[{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPSUPERGOLF\"]},{\"size\":8,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCHKBOXROTURAFUERA\"]}]},{\"columns\":[{\"size\":12,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCHKBOXIKE\"]}]}],\"fieldNames\":null}]}]}},{\"idubicacion\":2,\"calle\":\"Calle 10\",\"numero\":\"20\",\"cpData\":{\"idCp\":0,\"cp\":\"11290\",\"colonia\":\"Tehuantepec\",\"delegacion\":\"Miguel Hidalgo\",\"estado\":\"CDMX\",\"pais\":\"México\"},\"tipoInmueble\":0,\"tipoUso\":0,\"niveles\":0,\"descripcionConstruccion\":\"Edificio\",\"descripcionMedidaSeguridad\":\"Medidas de Seguridad Edificio\",\"valorInmueble\":85858500,\"primaNeta\":200000,\"aseguradoAdicional\":[{\"idPersona\":100,\"tipoPer\":217,\"extranjero\":1,\"rfc\":\"BEBI771212HCC\",\"nombreCompleto\":\"Ivan Benitez Hernández\",\"nombre\":\"Ivan\",\"appPaterno\":\"Benitez\",\"appMaterno\":\"Hernández\",\"idDenominacion\":null,\"codigo\":null},{\"idPersona\":200,\"tipoPer\":217,\"extranjero\":0,\"rfc\":\"FLDUBI991110JNN\",\"nombreCompleto\":\"Uriel Flores Díaz\",\"nombre\":\"Uriel\",\"appPaterno\":\"Flores\",\"appMaterno\":\"Díaz\",\"idDenominacion\":null,\"codigo\":null}],\"fields\":[{\"type\":\"label\",\"predefinedValue\":null,\"name\":\"PFTITULOGEN\",\"label\":\"SUMA ASEGURADA POR COBERTURA\",\"required\":false,\"readOnly\":false,\"showLabel\":true,\"visibilityExpression\":\"\",\"tip\":null,\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"\"},{\"type\":\"checkbox\",\"predefinedValue\":null,\"name\":\"PFCHKBOXIKE\",\"label\":\"Asistencia en el Hogar IKE\",\"required\":true,\"readOnly\":false,\"showLabel\":true,\"visibilityExpression\":\"\",\"tip\":null,\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPEDIFICIO\",\"label\":\"Edificio\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":\"\",\"tip\":\"Suma Asegurada de Edificio\",\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"moneda\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPCONTENIDOS\",\"label\":\"Contenidos\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":\"\",\"tip\":\"Suma Asegurada de Contenidos\",\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"moneda\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPPERDIDAR\",\"label\":\"Pérdida de Rentas\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":\"\",\"tip\":\"Suma Asegurada de Pérdida de Rentas\",\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"moneda\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPGESTRA\",\"label\":\"Gastos Extraordinarios\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":\"\",\"tip\":\"Suma Asegurada de Gastos Extraordinarios\",\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"moneda\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPEEM\",\"label\":\"Equipo Electrónico Móvil\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":\"\",\"tip\":\"Suma Asegurada de Equipo Electrónico Móvil\",\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"moneda\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPRBSECI\",\"label\":\"Robo (Sección I)\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":\"\",\"tip\":\"Suma Asegurada de Robo (Sección I)\",\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"moneda\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPRBSECII\",\"label\":\"Robo (Sección II)\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":\"\",\"tip\":\"Suma Asegurada de Robo (Sección II)\",\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"moneda\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPRBSECIII\",\"label\":\"Robo (Sección III)\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":\"\",\"tip\":\"Suma Asegurada de Robo (Sección III)\",\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"moneda\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPDINVAL\",\"label\":\"Dinero y Valores\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":\"\",\"tip\":\"Suma Asegurada de Dinero y Valores\",\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"moneda\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPRC\",\"label\":\"Responsabilidad Civil\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":\"\",\"tip\":\"Suma Asegurada de Responsabilidad Civil\",\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"moneda\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPEEC\",\"label\":\"Equipo Electrónico\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":\"\",\"tip\":\"Suma Asegurada de Equipo Electrónico\",\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"moneda\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPCIRSTALES\",\"label\":\"Rotura de Cristales\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":\"\",\"tip\":\"Suma Asegurada de Rotura de Cristales\",\"dataProviderName\":\"\",\"multiple\":\"\",\"options\":[],\"min\":\"0\",\"max\":\"\",\"minlength\":\"\",\"maxlength\":\"\",\"pattern\":\"\",\"clase\":\"moneda\"},{\"type\":\"checkbox\",\"predefinedValue\":\"false\",\"name\":\"PFCHKBOXROTURADENTRO\",\"label\":\"¿Ampara rotura - robo de palos de golf dentro de la ubicación?\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":null,\"tip\":null,\"dataProviderName\":null,\"multiple\":null,\"options\":null,\"min\":null,\"max\":null,\"minlength\":null,\"maxlength\":null,\"pattern\":null,\"clase\":\"suscriptor\"},{\"type\":\"checkbox\",\"predefinedValue\":\"false\",\"name\":\"PFCHKBOXROTURAFUERA\",\"label\":\"¿Ampara rotura - robo de palos de golf fuera de la ubicación?\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":null,\"tip\":null,\"dataProviderName\":null,\"multiple\":null,\"options\":null,\"min\":null,\"max\":null,\"minlength\":null,\"maxlength\":null,\"pattern\":null,\"clase\":\"suscriptor\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPPALOSGOLF\",\"label\":\"Palos de golf\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":null,\"tip\":\"Suma Asegurada de Palos de Golf\",\"dataProviderName\":null,\"multiple\":null,\"options\":null,\"min\":null,\"max\":null,\"minlength\":null,\"maxlength\":null,\"pattern\":null,\"clase\":\"moneda suscriptor\"},{\"type\":\"text\",\"predefinedValue\":null,\"name\":\"PFCAMPSUPERGOLF\",\"label\":\"Super Golf\",\"required\":false,\"readOnly\":false,\"showLabel\":false,\"visibilityExpression\":null,\"tip\":\"Suma Asegurada de Super Golf\",\"dataProviderName\":null,\"multiple\":null,\"options\":null,\"min\":null,\"max\":null,\"minlength\":null,\"maxlength\":null,\"pattern\":null,\"clase\":\"moneda suscriptor\"}],\"layout\":{\"rows\":[{\"columns\":[{\"size\":12,\"header\":\"Suma Asegurada por Cobertura\",\"accordion\":null,\"rows\":[{\"columns\":[{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPEDIFICIO\"]},{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPCONTENIDOS\"]},{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPPERDIDAR\"]}]},{\"columns\":[{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPGESTRA\"]},{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPCIRSTALES\"]},{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPRBSECI\"]}]},{\"columns\":[{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPRBSECII\"]},{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPRBSECIII\"]},{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPDINVAL\"]}]},{\"columns\":[{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPRC\"]},{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPEEC\"]},{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPEEM\"]}]},{\"columns\":[{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPPALOSGOLF\"]},{\"size\":8,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCHKBOXROTURADENTRO\"]}]},{\"columns\":[{\"size\":4,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCAMPSUPERGOLF\"]},{\"size\":8,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCHKBOXROTURAFUERA\"]}]},{\"columns\":[{\"size\":12,\"header\":null,\"accordion\":null,\"rows\":null,\"fieldNames\":[\"PFCHKBOXIKE\"]}]}],\"fieldNames\":null}]}]}}]}\n", UbicacionesResponse.class);

			respuesta.addProperty("code", ubicacionResponse.getCode());
			respuesta.addProperty("msg", ubicacionResponse.getMsg());

			System.out.println(ubicacionResponse.toString());
			final PortletSession psession = resourceRequest.getPortletSession();
			if (ubicacionResponse.getCode() == 0) {


				String ubicacionString = CotizadorModularUtil.objtoJson(ubicacionResponse);
				String auxNombre = "LIFERAY_SHARED_F=" + ubicacionResponse.getFolio() + "_C="
						+ ubicacionResponse.getCotizacion() + "_V=" + ubicacionResponse.getVersion()
						+ "_UBICACIONRESPONSE";

				System.out.println(".........................>2");
				System.out.println(ubicacionString);
				psession.setAttribute(auxNombre, ubicacionString, PortletSession.APPLICATION_SCOPE);

			}
			if (ubicacionResponse.getCode() == 5) {

				System.out.println("entre en codigo 5");

				String ubicacionString = CotizadorModularUtil.objtoJson(ubicacionResponse);
				String auxNombre = "LIFERAY_SHARED_F=" + ubicacionResponse.getFolio() + "_C="
						+ ubicacionResponse.getCotizacion() + "_V=" + ubicacionResponse.getVersion()
						+ "_UBICACIONRESPONSE";

				String auxNombre2 = "LIFERAY_SHARED_F=" + ubicacionResponse.getFolio() + "_C="
						+ ubicacionResponse.getCotizacion() + "_V=" + ubicacionResponse.getVersion()
						+ "_EXCEDELIMITES";

				psession.setAttribute(auxNombre2, 1, PortletSession.APPLICATION_SCOPE);
				psession.setAttribute(auxNombre, ubicacionString, PortletSession.APPLICATION_SCOPE);

				System.out.println("los nombres de variables son:");
				System.out.println(auxNombre);
				System.out.println(auxNombre2);
			}
			writer.write(respuesta.toString());
		} catch (/*CotizadorModularException*/ Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			respuesta.addProperty("code", 2);
			respuesta.addProperty("msg", "Error al consultar su información");
			writer.write(respuesta.toString());
		}
	}

}
