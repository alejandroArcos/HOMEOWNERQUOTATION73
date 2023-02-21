package HomeOwnerQuotation73.commands.action;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.cotizador.jsonformservice.JsonFormService;
import com.tokio.pa.cotizadorModularServices.Bean.Cliente;
import com.tokio.pa.cotizadorModularServices.Bean.CpData;
import com.tokio.pa.cotizadorModularServices.Bean.CpResponse;
import com.tokio.pa.cotizadorModularServices.Bean.InfoCotizacion;
import com.tokio.pa.cotizadorModularServices.Bean.ListaRegistro;
import com.tokio.pa.cotizadorModularServices.Bean.Registro;
import com.tokio.pa.cotizadorModularServices.Bean.Ubicacion;
import com.tokio.pa.cotizadorModularServices.Bean.UbicacionesResponse;
import com.tokio.pa.cotizadorModularServices.Constants.CotizadorModularServiceKey;
import com.tokio.pa.cotizadorModularServices.Enum.ModoCotizacion;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorGenerico;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorPaso1;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorPaso2;
import com.tokio.pa.cotizadorModularServices.Util.CotizadorModularUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import HomeOwnerQuotation73.constants.HomeOwnerQuotation73PortletKeys;


@Component(
		immediate = true,
		property = { 
				"javax.portlet.init-param.copy-request-parameters=true",
				"javax.portlet.name=" + HomeOwnerQuotation73PortletKeys.HOMEOWNERQUOTATION73,
				"mvc.command.name=/cotizadores/actionPaso2"
				},
		service = MVCActionCommand.class
		)

public class Paso2ActionCommand extends BaseMVCActionCommand {
	
	InfoCotizacion infCotizacion;
	User user;
	int idPerfilUser;
	
	@Reference
	CotizadorPaso1 _CMServicesP1;

	@Reference
	CotizadorPaso2 _CMServicesP2;
	
	@Reference
	CotizadorGenerico _CMServicesGenerico;
	
	@Reference
	JsonFormService _JsonFormService;
	
	private static final Log _log = LogFactoryUtil.getLog(Paso2ActionCommand.class);

	@Override
	protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Action Paso 2");
		
		llenaInfCotizacion(actionRequest);
		recuperaInfoUbicaciones(actionRequest);
		fLlenaCatalogos(actionRequest);
		recuperaSubGiroRiesgo(actionRequest);
		recuperaInfoSuma(actionRequest);
		recuperaInfoPaso1(actionRequest);
		recuperaInfoBajaEndoso(actionRequest);
		recuperaInfoCargaMasiva(actionRequest);

		String infCotJson = CotizadorModularUtil.objtoJson(infCotizacion);

		actionRequest.setAttribute("perfilSuscriptor", perfilSuscriptor());
		actionRequest.setAttribute("perfilJapones", perfilJapones());
		actionRequest.setAttribute("infCotJson", infCotJson);
		actionRequest.setAttribute("infCotizacion", infCotizacion);
		actionRequest.setAttribute("perfilMayorEjecutivo", perfilPermisosGeneral());
		
		//actionResponse.getRenderParameters().setValue("jspPage", "/cotizadorPaso2.jsp");
		actionResponse.getRenderParameters().setValue("jspPage", "/cotizadorPaso2.jsp");
	}
	
	private int perfilJapones() {
		try {
			switch (idPerfilUser) {
				case HomeOwnerQuotation73PortletKeys.PERFIL_JAPONES:
					return 1;
			}
			return 0;
		} catch (Exception e) {
			return 0;
		}
	}
	
	private void llenaInfCotizacion(ActionRequest actionRequest) {
		try {
			HttpServletRequest originalRequest = PortalUtil
					.getOriginalServletRequest(PortalUtil.getHttpServletRequest(actionRequest));

			user = (User) actionRequest.getAttribute(WebKeys.USER);
			idPerfilUser = (int) originalRequest.getSession().getAttribute("idPerfil");

			System.out.println("-------------------aqui-------------------------------");
			String inf = "";
			if (Validator.isNull(actionRequest.getAttribute("infoCotizacionString"))) {
				System.out.println("recupere de original");
				inf = originalRequest.getParameter("infoCotizacion");
				infCotizacion = CotizadorModularUtil.decodeURL(inf);
				System.out.println(inf);
			} else {
				System.out.println("recupere de render");
				Gson gson = new Gson();
				inf = (String) actionRequest.getAttribute("infoCotizacionString");
				infCotizacion = gson.fromJson(inf, InfoCotizacion.class);
				System.out.println(infCotizacion.toString());
				System.out.println(inf);
			}

			String nombreCotizador = "";

			System.err.println(infCotizacion.toString());

			switch (infCotizacion.getTipoCotizacion()) {
				case FAMILIAR:
					infCotizacion.setPantalla(HomeOwnerQuotation73PortletKeys.PANTALLA_FAMILIAR);
					nombreCotizador = HomeOwnerQuotation73PortletKeys.TITULO_FAMILIAR;
					break;
				default:
					infCotizacion.setPantalla("");
					nombreCotizador = "";
					break;
			}
			actionRequest.setAttribute("tituloCotizador", nombreCotizador);
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("------------------ llenaInfoCotizacion:");
			e.printStackTrace();
			SessionErrors.add(actionRequest, "errorConocido");
			actionRequest.setAttribute("errorMsg", "Error al cargar la cotización");
			SessionMessages.add(actionRequest, PortalUtil.getPortletId(actionRequest)
					+ SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE);
		}
	}

	private void recuperaInfoUbicaciones(ActionRequest actionRequest) {
		try {
			final PortletSession psession = actionRequest.getPortletSession();
			Map<Integer, Integer> relacionUbicaciones = new HashMap<Integer, Integer>();

			Gson gson = new Gson();
			String auxNombre = "LIFERAY_SHARED_F=" + infCotizacion.getFolio() + "_C="
					+ infCotizacion.getCotizacion() + "_V=" + infCotizacion.getVersion()
					+ "_UBICACIONRESPONSE";

			System.out.println("auxNombre" + auxNombre);
			String ubicacion = (String) psession.getAttribute(auxNombre,
					PortletSession.APPLICATION_SCOPE);

			System.out.println(ubicacion);

			UbicacionesResponse ubi = gson.fromJson(ubicacion, UbicacionesResponse.class);
			
			//Modificaciones temporales
			/*
			UbicacionesResponse newUbi = new UbicacionesResponse();
			List<Ubicacion> ubiTemp = new ArrayList<Ubicacion>();
			Ubicacion ubiAux = new Ubicacion();
			ubiAux.setCpData(ubi.getUbicaciones().get(0).getCpData());
			ubiAux.setField(ubi.getUbicaciones().get(0).getField());
			ubiAux.setLayouts(ubi.getUbicaciones().get(0).getLayouts());
			ubiAux.getCpData().setCp(null);
			ubiAux.getCpData().setEstado(null);
			ubiAux.getCpData().setDelegacion(null);
			ubiAux.setIdubicacion(1);
			ubiTemp.add(ubiAux);
			newUbi.setUbicaciones(ubiTemp);
			*/
			
			JsonArray asegurados = new JsonArray();
			JsonArray beneficiarios = new JsonArray();

			if (Validator.isNotNull(ubi)) {

				infCotizacion.setNoUbicaciones(ubi.getUbicaciones().size());
				//infCotizacion.setNoUbicaciones(1);

				int auxCont = 1;
				
				for (Ubicacion u : ubi.getUbicaciones()) {
					
					JsonArray auxAsegurados = new JsonArray();
					JsonArray auxBeneficiarios = new JsonArray();
					
					if(Validator.isNotNull(u.getAseguradoAdicional())) {
						for(Cliente c : u.getAseguradoAdicional()) {
							JsonObject auxObj = gson.fromJson(gson.toJson(c), JsonObject.class);
							auxAsegurados.add(auxObj);
						}
						
						asegurados.add(auxAsegurados);
					}
					
					if(Validator.isNotNull(u.getBeneficiarioPreferente())) {
						for(Cliente c : u.getBeneficiarioPreferente()) {
							JsonObject auxObj = gson.fromJson(gson.toJson(c), JsonObject.class);
							auxBeneficiarios.add(auxObj);
						}
						
						beneficiarios.add(auxBeneficiarios);
					}
					
					relacionUbicaciones.put(auxCont, u.getIdubicacion());
					auxCont++;
				}

				generaAcordeones(actionRequest, ubi);
				generaListColonias(actionRequest, ubi);

				actionRequest.setAttribute("ubicacion", ubi);
				actionRequest.setAttribute("relacionUbicaciones", relacionUbicaciones);
				actionRequest.setAttribute("relacionAsegurados", asegurados);
				actionRequest.setAttribute("relacionBeneficiarios", beneficiarios);
				

			} else {
				infCotizacion.setNoUbicaciones(0);
				SessionErrors.add(actionRequest, "errorConocido");
				actionRequest.setAttribute("errorMsg", "Error al traer su informacion");
				SessionMessages.add(actionRequest, PortalUtil.getPortletId(actionRequest)
						+ SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE);
				
				
			}
			
			actionRequest.setAttribute("folioCotizacion", infCotizacion.getFolio());
			actionRequest.setAttribute("versionCotizacion", infCotizacion.getVersion());

		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("------------------ info ubicaciones:");
			e.printStackTrace();
			SessionErrors.add(actionRequest, "errorConocido");
			actionRequest.setAttribute("errorMsg", "Error al cargar las ubicaciones");
			SessionMessages.add(actionRequest, PortalUtil.getPortletId(actionRequest)
					+ SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE);
			
			actionRequest.setAttribute("folioCotizacion", infCotizacion.getFolio());
			actionRequest.setAttribute("versionCotizacion", infCotizacion.getVersion());
		}
	}
	
	private void generaAcordeones(ActionRequest actionRequest, UbicacionesResponse ubi) {
		try {
			Gson gson = new Gson();
			Map<Integer, String> jsonFiels = new HashMap<Integer, String>();

			String dataProvide = gson.toJson(ubi.getDataProviders());
			int i = 1;
			for (Ubicacion ubicacion : ubi.getUbicaciones()) {

				String jsonformfields = "{\"fields\":" + ubicacion.getField() + "}";
				String jsonlayout = ubicacion.getLayouts();
				String jsonDataProviders = "{\"dataProviders\":" + dataProvide + "}";
				String u = ubicacion.getIdubicacion() + "";
				System.out.println(jsonformfields);
				String htmlUbicacion = _JsonFormService.parse(jsonformfields, jsonlayout,
						jsonDataProviders, u);
				jsonFiels.put(i, htmlUbicacion);
				i++;
			}
			actionRequest.setAttribute("jsonFiels", jsonFiels);
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("------------------ generaAcordeones");
			e.printStackTrace();
			SessionErrors.add(actionRequest, "errorConocido");
			actionRequest.setAttribute("errorMsg", "Error al cargar las ubicaciones");
			SessionMessages.add(actionRequest, PortalUtil.getPortletId(actionRequest)
					+ SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE);
		}

	}

	private void generaListColonias(ActionRequest actionRequest, UbicacionesResponse ubi) {
		// TODO Auto-generated method stub
		try {
			ArrayList<List<CpData>> colonias = new ArrayList<List<CpData>>();
			List<CpData> dataVacia = null;
			for (Ubicacion u : ubi.getUbicaciones()) {
				String cp = u.getCpData().getCp();
				if (Validator.isNotNull(cp)) {
					CpResponse cpdata = _CMServicesP2.getCP(cp, user.getScreenName(),
							infCotizacion.getPantalla());
					if (cpdata.getCode() == 0) {
						colonias.add(cpdata.getListaCpData());
					} else {
						colonias.add(dataVacia);
					}
				} else {
					colonias.add(dataVacia);
				}
			}
			System.out.println("colonias: " + colonias);
			actionRequest.setAttribute("colonias", colonias);
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("------------------ Colonias");
			e.printStackTrace();
			SessionErrors.add(actionRequest, "errorConocido");
			actionRequest.setAttribute("errorMsg", "Error al cargar las ubicaciones");
			SessionMessages.add(actionRequest, PortalUtil.getPortletId(actionRequest)
					+ SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE);
		}
	}

	private void fLlenaCatalogos(ActionRequest actionRequest) {
		ListaRegistro listaNiveles = fGetCatalogos(CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
				CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
				CotizadorModularServiceKey.LIST_CAT_NIVELES,
				CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
				infCotizacion.getPantalla(), actionRequest);

		actionRequest.setAttribute("listaNiveles", listaNiveles.getLista());

			ListaRegistro listaInmuebles = fGetCatalogos(
					CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_INMUEBLE,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					infCotizacion.getPantalla(), actionRequest);

			ListaRegistro listaUso = fGetCatalogos(CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_TPOUSO,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					infCotizacion.getPantalla(), actionRequest);
			
			ListaRegistro listaTechos = fGetCatalogos(CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_TECHOS,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					infCotizacion.getPantalla(), actionRequest);
			
			ListaRegistro listaMuros = fGetCatalogos(CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_MUROS,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					infCotizacion.getPantalla(), actionRequest);
			
			ListaRegistro listaMedidas = fGetCatalogos(CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_MEDIDAS_SEG,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					infCotizacion.getPantalla(), actionRequest);
			
			ListaRegistro listaCatDenominacion = fGetCatalogos(
					CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_DENOMINACION,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					infCotizacion.getPantalla(), actionRequest);

			actionRequest.setAttribute("listaInmuebles", listaInmuebles.getLista());
			actionRequest.setAttribute("listaUso", listaUso.getLista());
			actionRequest.setAttribute("listaTechos", listaTechos.getLista());
			actionRequest.setAttribute("listaMuros", listaMuros.getLista());
			actionRequest.setAttribute("listaMedidas", listaMedidas.getLista());
			actionRequest.setAttribute("listaCatDenominacion", listaCatDenominacion.getLista());

	}

	private ListaRegistro fGetCatalogos(int p_rownum, String p_tiptransaccion, String p_codigo,
			int p_activo, String p_usuario, String p_pantalla, ActionRequest actionRequest) {
		try {
			ListaRegistro lr = _CMServicesGenerico.getCatalogo(p_rownum, p_tiptransaccion, p_codigo,
					p_activo, p_usuario, p_pantalla);

			// lr.getLista().sort(Comparator.comparing(Registro::getDescripcion));
			return lr;
		} catch (Exception e) {
			System.err.print("----------------- error en traer los catalogos");
			e.printStackTrace();
			SessionErrors.add(actionRequest, "errorConocido");
			actionRequest.setAttribute("errorMsg", "Error en catalogos");
			SessionMessages.add(actionRequest, PortalUtil.getPortletId(actionRequest)
					+ SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE);
			return null;
		}
	}

	private void recuperaSubGiroRiesgo(ActionRequest actionRequest) {
		final PortletSession psession = actionRequest.getPortletSession();

		String auxNombre = "LIFERAY_SHARED_F=" + infCotizacion.getFolio() + "_C="
				+ infCotizacion.getCotizacion() + "_V=" + infCotizacion.getVersion()
				+ "_SUBGIRORIESGO";

		if (Validator
				.isNotNull(psession.getAttribute(auxNombre, PortletSession.APPLICATION_SCOPE))) {
			int subGiroRiesgo = (int) psession.getAttribute(auxNombre,
					PortletSession.APPLICATION_SCOPE);

			actionRequest.setAttribute("subGiroRiesgo", subGiroRiesgo);

		} else {
			SessionErrors.add(actionRequest, "errorConocido");
			actionRequest.setAttribute("errorMsg", "Error al recuperar su información");
			SessionMessages.add(actionRequest, PortalUtil.getPortletId(actionRequest)
					+ SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE);
		}
	}
	
	private void recuperaInfoCargaMasiva(ActionRequest actionRequest) {
		
		String auxNombre = "LIFERAY_SHARED_F=" + infCotizacion.getFolio() + "_C="
				+ infCotizacion.getCotizacion() + "_V=" + infCotizacion.getVersion() + "_CARGAMASIVA";

		final PortletSession psession = actionRequest.getPortletSession();
		if (Validator
				.isNotNull(psession.getAttribute(auxNombre, PortletSession.APPLICATION_SCOPE))) {
			
			boolean infoCargaMasiva = (boolean) psession.getAttribute(auxNombre,
					PortletSession.APPLICATION_SCOPE);

			System.out.println("Carga Masiva: " + infoCargaMasiva);
			
			actionRequest.setAttribute("infoCargaMasiva", infoCargaMasiva);
		}
		
	}

	private void recuperaInfoSuma(ActionRequest actionRequest) {
		try {
			final PortletSession psession = actionRequest.getPortletSession();

			String auxNombre = "LIFERAY_SHARED_F=" + infCotizacion.getFolio() + "_C="
					+ infCotizacion.getCotizacion() + "_V=" + infCotizacion.getVersion()
					+ "_ACEPTASUSCRIPCION";
			String auxNombre2 = "LIFERAY_SHARED_F=" + infCotizacion.getFolio() + "_C="
					+ infCotizacion.getCotizacion() + "_V=" + infCotizacion.getVersion()
					+ "_EXCEDELIMITES";

			System.out.println("las variables al recuperar son:");
			System.out.println(auxNombre);
			System.out.println(auxNombre2);

			if (Validator.isNotNull(
					psession.getAttribute(auxNombre, PortletSession.APPLICATION_SCOPE))) {
				int aceptaSuscripcion = (int) psession.getAttribute(auxNombre,
						PortletSession.APPLICATION_SCOPE);

				actionRequest.setAttribute("aceptaSuscripcion", aceptaSuscripcion);

			}
			if (Validator.isNotNull(
					psession.getAttribute(auxNombre, PortletSession.APPLICATION_SCOPE))) {
				int excedeLimites = (int) psession.getAttribute(auxNombre2,
						PortletSession.APPLICATION_SCOPE);

				actionRequest.setAttribute("excedeLimites", excedeLimites);

			}
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	private int perfilSuscriptor() {
		try {
			switch (idPerfilUser) {
				case HomeOwnerQuotation73PortletKeys.PERFIL_SUSCRIPTORJR:
					return 1;
				case HomeOwnerQuotation73PortletKeys.PERFIL_SUSCRIPTORSR:
					return 1;
				case HomeOwnerQuotation73PortletKeys.PERFIL_SUSCRIPTORMR:
					return 1;
			}
			return 0;
		} catch (Exception e) {
			// TODO: handle exception
			return 0;
		}
	}

	private boolean perfilPermisosGeneral() {
		try {
			switch (idPerfilUser) {
				case HomeOwnerQuotation73PortletKeys.PERFIL_EJECUTIVO:
					return true;
				case HomeOwnerQuotation73PortletKeys.PERFIL_SUSCRIPTORJR:
					return true;
				case HomeOwnerQuotation73PortletKeys.PERFIL_SUSCRIPTORSR:
					return true;
				case HomeOwnerQuotation73PortletKeys.PERFIL_SUSCRIPTORMR:
					return true;
			}
			return false;
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
	}

	private void recuperaInfoPaso1(ActionRequest actionRequest) {
		String auxNombre = "LIFERAY_SHARED_F=" + infCotizacion.getFolio() + "_C="
				+ infCotizacion.getCotizacion() + "_V=" + infCotizacion.getVersion() + "_DATOSP1";

		final PortletSession psession = actionRequest.getPortletSession();
		if (Validator
				.isNotNull(psession.getAttribute(auxNombre, PortletSession.APPLICATION_SCOPE))) {
			String infoP1 = (String) psession.getAttribute(auxNombre,
					PortletSession.APPLICATION_SCOPE);

			System.out.println("infoP1 del paso 2 : " + infoP1);

			actionRequest.setAttribute("infoP1", infoP1);
			
			JsonObject objAux = new JsonObject();
			Gson gson = new Gson();
			
			objAux = gson.fromJson(infoP1, JsonObject.class);
			
			try {
			
				Cliente cliente = _CMServicesP1.getCotizadorData(infCotizacion.getFolio(),
						infCotizacion.getCotizacion(), infCotizacion.getVersion(),
						user.getScreenName(), infCotizacion.getPantalla()).getDatosCotizacion().getDatosCliente();
				
				String nombreCliente = cliente
						.getNombre().replace("\"", "\\\"");
				
				_log.info(nombreCliente);
				
				if(cliente.getTipoPer() == 218) {
				
					ListaRegistro listaCatDenominacion = fGetCatalogos(
							CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
							CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
							CotizadorModularServiceKey.LIST_CAT_DENOMINACION,
							CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
							infCotizacion.getPantalla(), actionRequest);
					
					List<Registro> lista = listaCatDenominacion.getLista();
					String denominacion = "";
					
					for(Registro r : lista) {
						if(r.getIdCatalogoDetalle() == cliente.getIdDenominacion()) {
							denominacion = r.getDescripcion();
						}
					}
					
					actionRequest.setAttribute("nombreContratante", cliente.getNombre() + ", " + denominacion);
				}
				else {
					actionRequest.setAttribute("nombreContratante", cliente.getNombre() + " " + cliente.getAppPaterno() 
							+ " " + cliente.getAppMaterno());
				}
				
				System.out.println(cliente);
			}
			catch(Exception e) {
				e.printStackTrace();
				
				actionRequest.setAttribute("nombreContratante", "Error");
			}
			

		}
	}
	
	
	private void recuperaInfoBajaEndoso(ActionRequest actionRequest) {
		// TODO Auto-generated method stub
		
		if(infCotizacion.getModo().equals(ModoCotizacion.BAJA_ENDOSO)){
			String auxNombre = "LIFERAY_SHARED_F=" + infCotizacion.getFolio() + "_C="
					+ infCotizacion.getCotizacion() + "_V=" + infCotizacion.getVersion() + "_DATOSP1";

			final PortletSession psession = actionRequest.getPortletSession();
			if (Validator
					.isNotNull(psession.getAttribute(auxNombre, PortletSession.APPLICATION_SCOPE))) {
				String infoP1 = (String) psession.getAttribute(auxNombre,
						PortletSession.APPLICATION_SCOPE);

				System.out.println("infoP1 del paso 2 : " + infoP1);

				actionRequest.setAttribute("infoP1", infoP1);

			}

		}
		
	}

}
