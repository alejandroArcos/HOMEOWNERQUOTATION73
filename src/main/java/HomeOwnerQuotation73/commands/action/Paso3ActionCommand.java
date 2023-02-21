package HomeOwnerQuotation73.commands.action;

import com.google.gson.Gson;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.pa.cotizadorModularServices.Bean.CaratulaBajaDatosCaratula;
import com.tokio.pa.cotizadorModularServices.Bean.CaratulaBajaDatosGeneral;
import com.tokio.pa.cotizadorModularServices.Bean.CaratulaBajaUbicaciones;
import com.tokio.pa.cotizadorModularServices.Bean.CaratulaResponse;
import com.tokio.pa.cotizadorModularServices.Bean.Cliente;
import com.tokio.pa.cotizadorModularServices.Bean.CotizadorDataResponse;
import com.tokio.pa.cotizadorModularServices.Bean.InfoAuxPaso1;
import com.tokio.pa.cotizadorModularServices.Bean.InfoCotizacion;
import com.tokio.pa.cotizadorModularServices.Bean.ListaRegistro;
import com.tokio.pa.cotizadorModularServices.Bean.Registro;
import com.tokio.pa.cotizadorModularServices.Bean.SimpleResponse;
import com.tokio.pa.cotizadorModularServices.Constants.CotizadorModularServiceKey;
import com.tokio.pa.cotizadorModularServices.Enum.ModoCotizacion;
import com.tokio.pa.cotizadorModularServices.Enum.TipoCotizacion;
import com.tokio.pa.cotizadorModularServices.Exception.CotizadorModularException;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorGenerico;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorPaso1;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorPaso3;
import com.tokio.pa.cotizadorModularServices.Util.CotizadorModularUtil;

import java.text.DecimalFormat;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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
				"mvc.command.name=/cotizadores/actionPaso3"
				},
		service = MVCActionCommand.class
		)

public class Paso3ActionCommand extends BaseMVCActionCommand {
	
	@Reference
	CotizadorPaso3 _ServicePaso3;
	@Reference
	CotizadorGenerico _ServiceGenerico;
	@Reference
	CotizadorPaso1 _CMServicesP1;

	User user;
	int idPerfilUser;

	InfoCotizacion infCotizacion = new InfoCotizacion();
	InfoAuxPaso1 infoPaso1 = new InfoAuxPaso1();

	double minPrima = 0;
	double tpoCambio = 0;

	String tblTitulos = "";
	String tblUbicaciones = "";
	String tblTotales = "";

	Gson gson = new Gson();
	CaratulaResponse caratulaResponse = new CaratulaResponse();

	@Override
	protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
		// TODO Auto-generated method stub
		
		SessionMessages.add(actionRequest, PortalUtil.getPortletId(actionRequest)
				+ SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE);
		HttpServletRequest originalRequest = PortalUtil
				.getOriginalServletRequest(PortalUtil.getHttpServletRequest(actionRequest));
		user = (User) actionRequest.getAttribute(WebKeys.USER);
		idPerfilUser = (int) originalRequest.getSession().getAttribute("idPerfil");

		llenaInfoCotizacion(actionRequest);
		recuperaInfoPaso1(actionRequest);
		validaModoCotizacion(actionRequest);

		String infoCotJson = CotizadorModularUtil.objtoJson(infCotizacion);
		
		llenaTipoCambio();
		llenaCatalogos(actionRequest);
		
		fLlenaDatosTabla(actionRequest);
		if (!isBajaEndoso()) {
			if(Validator.isNotNull(infoPaso1.getSubEstado())){
				if(!infoPaso1.getSubEstado().equals( HomeOwnerQuotation73PortletKeys.COTIZADO_SUSCRIPTOR)){		
					validaPrimaminima(actionRequest);	
				}
			}else{
				validaPrimaminima(actionRequest);
			}
			
		}

		
		actionRequest.setAttribute("minPrima", minPrima);
		actionRequest.setAttribute("tpoCambio", tpoCambio);
		actionRequest.setAttribute("infoCotJson", infoCotJson);
		actionRequest.setAttribute("idPerfilUser", idPerfilUser);
		actionRequest.setAttribute("infCotizacion", infCotizacion);
		
		actionRequest.setAttribute("mailUser", Base64.getEncoder().encodeToString(user.getEmailAddress().toString().getBytes()));
		caratulaResponse.setEmail(Base64.getEncoder().encodeToString(caratulaResponse.getEmail().toString().getBytes()));
		
		actionRequest.setAttribute("caratulaResponse", caratulaResponse);
		
		actionRequest.setAttribute("infP1", CotizadorModularUtil.objtoJson( infoPaso1 ));

		actionRequest.setAttribute("botonesCaratula",
				botonesCaratula(infoPaso1.getSubEstado(), idPerfilUser));
		
		actionRequest.setAttribute("perfilSuscriptor", perfilSuscriptor());
		actionRequest.setAttribute("perfilJapones", perfilJapones());

		
		actionResponse.setRenderParameter("jspPage", "/cotizadorPaso3.jsp");
	}
	
	private void llenaTipoCambio(){
		try {
			tpoCambio = _ServicePaso3.getTipoCambio().getTipoCambio();
		} catch (CotizadorModularException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void llenaCatalogos(ActionRequest actionRequest){
		ListaRegistro listaPrimaMinima = fGetCatalogos(CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
				CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
				CotizadorModularServiceKey.LIST_CAT_MIN_PRI,
				CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
				infCotizacion.getPantalla());

		ListaRegistro listaMotivoRechazo = fGetCatalogos(
				CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
				CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
				CotizadorModularServiceKey.LIST_CAT_MOTI_RECHAZO,
				CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
				infCotizacion.getPantalla());

	

		Registro r = null;
		if(isEndoso()){
			if(infCotizacion.getTipoCotizacion().equals(TipoCotizacion.EMPRESARIAL)){
				r = listaPrimaMinima.getLista().stream().filter(r2 -> "PRIMINPEE".equals(r2.getCodigo()))
						.findAny().orElse(new Registro());
			}else{
				r = listaPrimaMinima.getLista().stream().filter(r2 -> "PRIMINPFE".equals(r2.getCodigo()))
						.findAny().orElse(new Registro());
			}			
		}else{
			if(infCotizacion.getTipoCotizacion().equals(TipoCotizacion.EMPRESARIAL)){
				r = listaPrimaMinima.getLista().stream().filter(r2 -> "PRIMINPE".equals(r2.getCodigo()))
						.findAny().orElse(new Registro());
			}else{
				r = listaPrimaMinima.getLista().stream().filter(r2 -> "PRIMINPF".equals(r2.getCodigo()))
						.findAny().orElse(new Registro());
			}
		
		}
		minPrima = Double.parseDouble(r.getValor());
		actionRequest.setAttribute("motivoRechazo", listaMotivoRechazo.getLista());
	}
	

	private ListaRegistro fGetCatalogos(int p_rownum, String p_tiptransaccion, String p_codigo,
			int p_activo, String p_usuario, String p_pantalla) {
		try {
			ListaRegistro list = _ServiceGenerico.getCatalogo(p_rownum, p_tiptransaccion, p_codigo,
					p_activo, p_usuario, p_pantalla);
			list.getLista().sort(Comparator.comparing(Registro::getDescripcion));
			return list;
			/* return null; */
		} catch (Exception e) {
			return null;
		}
	}

	private void fLlenaDatosTabla(ActionRequest actionRequest) {
		// caratulaResponse.setCode(3);
		if (caratulaResponse.getCode() == 0) {
			System.out.println("INFO CORRECTA");
		} else {
			System.out.println("INFO INCORRECTA");
			SessionErrors.add(actionRequest, "errorServicio");
		}
	}

	public int botonesCaratula(String subEstado, int idPerfil) {
		if (Validator.isNotNull(subEstado)) {
			if ((idPerfil < 4 || idPerfil == 25) && ((subEstado.trim()
					.equals(HomeOwnerQuotation73PortletKeys.COTIZADO_SUSCRIPTOR))
					|| (subEstado.trim().equals(HomeOwnerQuotation73PortletKeys.RECHAZO_VBA_482)))) {
				return 1;
			}
			if ((idPerfil > 3) && ((subEstado.trim().equals(HomeOwnerQuotation73PortletKeys.EDO_SUBGIRO))
					|| (subEstado.trim().equals(HomeOwnerQuotation73PortletKeys.EXEDE_LIMITES))
					|| (subEstado.trim().equals(HomeOwnerQuotation73PortletKeys.REVIRE_SUSCRIPTOR)))) {
				return 2;
			}
		}
		return 0;
	}

	private void fGetCaratula(ActionRequest actionRequest) {
		try {
			String cur_version = String.valueOf(infCotizacion.getVersion());
			caratulaResponse = _ServicePaso3.getCaratula((int) infCotizacion.getCotizacion(),
					cur_version, user.getScreenName(), infCotizacion.getPantalla());
			// if caratulaResponse.getPrimaNeta()
		} catch (Exception e) {
			// TODO: handle exception
			caratulaResponse = new CaratulaResponse();
		}
	}

	private void llenaInfoCotizacion(ActionRequest actionRequest) {

		try {
			HttpServletRequest originalRequest = PortalUtil
					.getOriginalServletRequest(PortalUtil.getHttpServletRequest(actionRequest));

			String inf = originalRequest.getParameter("infoCotizacion");

			String nombreCotizador = "";
			if (Validator.isNotNull(inf)) {
				infCotizacion = CotizadorModularUtil.decodeURL(inf);
			} else {
				infCotizacion = new InfoCotizacion();
			}
			
			
//			auxRenovacion();

			System.out.println("-----------------------------------------");
			System.err.println("inf: " + infCotizacion.toString());
			
			nombreCotizador = "Cotizador Paquete Familiar";
			actionRequest.setAttribute("tituloCotizador", nombreCotizador);
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("------------------ llenaInfoCotizacion:");
			SessionErrors.add(actionRequest, "errorServicios");
			e.printStackTrace();
		}

	}

	private void validaModoCotizacion(ActionRequest actionRequest) {
		switch (infCotizacion.getModo()) {
			case FACTURA_492:
				actionRequest.setAttribute("Leg492", "factura");
				fGetCaratula(actionRequest);
				break;
			case ALTA_ENDOSO:
				actionRequest.setAttribute("dBtns", "d-none");
				fGetCaratula(actionRequest);
				break;
			case EDITAR_ALTA_ENDOSO:
				actionRequest.setAttribute("dBtns", "d-none");
				fGetCaratula(actionRequest);
				break;
			case BAJA_ENDOSO:
				actionRequest.setAttribute("dBtns", "d-none");
				caratulaBajaEnsos(actionRequest);
				break;
			case EDITAR_BAJA_ENDOSO:
				actionRequest.setAttribute("dBtns", "d-none");
				caratulaBajaEnsos(actionRequest);
				break;
			case RENOVACION_AUTOMATICA:
				actionRequest.setAttribute("dBtns", "d-none");
				actualizaInfoRenovacion();
				recuperaInfoPaso1(actionRequest);
				fGetCaratula(actionRequest);
				break;
			case EDITAR_RENOVACION_AUTOMATICA:
				actionRequest.setAttribute("dBtns", "d-none");
				infCotizacion.setModo(ModoCotizacion.RENOVACION_AUTOMATICA);
				fGetCaratula(actionRequest);
			case CONSULTAR_RENOVACION_AUTOMATICA:
				actionRequest.setAttribute("dBtns", "d-none");
				fGetCaratula(actionRequest);
			default:
				fGetCaratula(actionRequest);
				
				break;
		}
			
	}
	
	private void actualizaInfoRenovacion(){
		try {
			SimpleResponse respuesta = _ServicePaso3.GuardarCotizacionRenovacion((int)infCotizacion.getCotizacion(), 
					infCotizacion.getVersion(), user.getScreenName(), infCotizacion.getPantalla());
			infCotizacion.setCotizacion(respuesta.getCotizacion());
			infCotizacion.setFolio(Long.parseLong((respuesta.getFolio())));
			infCotizacion.setVersion(respuesta.getVersion());
		} catch (CotizadorModularException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void recuperaInfoPaso1(ActionRequest actionRequest) {
		try {
			final PortletSession psession = actionRequest.getPortletSession();
			
			Gson gson = new Gson();
			String auxNombre = "LIFERAY_SHARED_F=" + infCotizacion.getFolio() + "_C="
					+ infCotizacion.getCotizacion() + "_V=" + infCotizacion.getVersion()
					+ "_DATOSP1";
			

			String datosP1 = "";
			if(isRenovacion()){
				datosP1 = infop1Aux( actionRequest);
			}else{
				datosP1 =  (String) psession.getAttribute(auxNombre,
					PortletSession.APPLICATION_SCOPE);

			}
					
					
			

			if (Validator.isNotNull(datosP1)) {
				System.err.println("SESION RESPONSE: " + datosP1);

				infoPaso1 = gson.fromJson(datosP1, InfoAuxPaso1.class);
				actionRequest.setAttribute("tipoMoneda", infoPaso1.getMonedaSeleccionada());
			} else {
				System.err.println("CAMBIAR VALOR COTIZACION");
				infCotizacion.setModo(ModoCotizacion.ERROR);
			}
			
			Cliente cliente = _CMServicesP1.getCotizadorData(infCotizacion.getFolio(),
					infCotizacion.getCotizacion(), infCotizacion.getVersion(),
					user.getScreenName(), infCotizacion.getPantalla()).getDatosCotizacion().getDatosCliente();
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

		} catch (Exception e) {
			// TODO: handle exception
			infoPaso1 = new InfoAuxPaso1();
			e.printStackTrace();
			SessionErrors.add(actionRequest, "errorConocido");
			actionRequest.setAttribute("errorMsg", "Error al cargar las ubicaciones");
			SessionMessages.add(actionRequest, PortalUtil.getPortletId(actionRequest)
					+ SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE);
		}
	}
	
	private ListaRegistro fGetCatalogos(int p_rownum, String p_tiptransaccion, String p_codigo,
			int p_activo, String p_usuario, String p_pantalla, ActionRequest actionRequest) {
		try {
			ListaRegistro lr = _ServiceGenerico.getCatalogo(p_rownum, p_tiptransaccion, p_codigo,
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

	private void validaPrimaminima(ActionRequest actionRequest) {
		System.out.println("caratulaResponse.getPrimaNeta(): " + caratulaResponse.getPrimaNeta());
		System.out.println("minPrima: " + minPrima);
		
		double auxPrimaMin = 0;

		String tipoMoneda = infoPaso1.getMonedaSeleccionada();
		if(tipoMoneda.equals("1")){
			auxPrimaMin = minPrima;
		}else{
			auxPrimaMin = minPrima / tpoCambio;
		}
		
		if (caratulaResponse.getPrimaNeta() < auxPrimaMin) {
			System.out.println("APLICAR PRIMA MINIMA");
			String cur_version = String.valueOf(infCotizacion.getVersion());

			try {
				caratulaResponse = _ServicePaso3.GetCaratulaPrimaObjetivo(
						(int) infCotizacion.getCotizacion(), cur_version, auxPrimaMin,
						user.getScreenName(), infCotizacion.getPantalla());
				SessionErrors.add(actionRequest, "errorConocido");
				actionRequest.setAttribute("errorMsg",
						"Se ha aplicado la prima mínima del producto");
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	private boolean isEndoso() {
		switch (infCotizacion.getModo()) {
			case ALTA_ENDOSO:
				return true;
			case EDITAR_ALTA_ENDOSO:
				return true;
			case BAJA_ENDOSO:
				return true;
			case EDITAR_BAJA_ENDOSO:
				return true;
			default:
				return false;
		}

	}

	private boolean isBajaEndoso() {
		switch (infCotizacion.getModo()) {
			case BAJA_ENDOSO:
				return true;
			case EDITAR_BAJA_ENDOSO:
				return true;
			default:
				return false;
		}

	}

	void caratulaBajaEnsos(ActionRequest actionRequest) {

		try {
			/**
			 * Consumo el servicio
			 */
			CaratulaBajaDatosGeneral bs = _ServicePaso3.GetCaratulaEndoso(
					infCotizacion.getCotizacion() + "", infCotizacion.getVersion(),
					user.getScreenName(), infCotizacion.getPantalla());

			/**
			 * ordeno por ubicaciones
			 */
			for (CaratulaBajaDatosCaratula dc : bs.getDatosCaratula()) {
				dc.getUbicaciones()
						.sort(Comparator.comparing(CaratulaBajaUbicaciones::getUbicacion));
			}
			
			
			/**
			 * hacemos un map por contenedor llave principal
			 */
			Map<String, List<CaratulaBajaDatosCaratula>> datosCara = bs.getDatosCaratula().stream()
					.collect(Collectors.groupingBy(CaratulaBajaDatosCaratula::getContenedor));
			
			int totUbi = bs.getDatosCaratula().get(0).getUbicaciones().size();

	        Map<Integer, Double> totUbica = new HashMap<>(); 
	        
	        for (CaratulaBajaDatosCaratula dca : bs.getDatosCaratula()) {
				for (CaratulaBajaUbicaciones ubic : dca.getUbicaciones()) {
					if(!totUbica.containsKey(ubic.getUbicacion())){
						totUbica.put(ubic.getUbicacion(), 0.0);
					}
					String auxPrima = ubic.getPrima().replace("$", "").replace(",", "");
					try {
						double nv = totUbica.get(ubic.getUbicacion()) + Double.parseDouble(auxPrima);
						totUbica.put(ubic.getUbicacion(), nv);
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			}
			
	    
	        
	        for (Entry<Integer, Double> entry : totUbica.entrySet()) {
	            System.out.println(entry.getKey() + ":" + entry.getValue().toString());
	        }
	        
			String tablaBajasEndoso = "<table id=\"tblendbaja\" class=\"customTable w-100\" > <thead> <tr>";

			tablaBajasEndoso += generatitulos(bs.getDatosCaratula().get(0).getUbicaciones());

			for (Map.Entry<String, List<CaratulaBajaDatosCaratula>> entry : datosCara.entrySet()) {
				System.out.println(entry.getKey());
				tablaBajasEndoso += "<tr><td class=\"text-center font-weight-bold tb1\">" + entry.getKey() + "</td>";
				for (int i = 0; i <= totUbi; i++) {
					String adCls = (i < totUbi) ? "tb2" : "tb3";
					tablaBajasEndoso += "<td class=\" " + adCls + "\">&nbsp;</td>";
				}
				tablaBajasEndoso += "</tr>";
				tablaBajasEndoso += generaInfoTbl(entry.getValue());
				tablaBajasEndoso += "</tr>";
			}

			tablaBajasEndoso += "</table>";
			System.out.println(tablaBajasEndoso);
			actionRequest.setAttribute("tablaBajasEndoso", tablaBajasEndoso);
			
			System.out.println(bs.getPrimaNeta());
			caratulaResponse.setPrimaNeta((float) bs.getPrimaNeta());
			caratulaResponse.setRecargo((float) bs.getRecargo());
			caratulaResponse.setGastos((float) bs.getGastos());
			caratulaResponse.setIva((float) bs.getIva());
			caratulaResponse.setTotal((float) bs.getTotal());
			caratulaResponse.setEmail(bs.getEmail());
		} catch (CotizadorModularException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	String generatitulos(List<CaratulaBajaUbicaciones> ubicaciones) {
		String titulos = "<th class=\"tb1\"> Prima a devolver </th>";
		for (CaratulaBajaUbicaciones caratulaBajaUbicaciones : ubicaciones) {
			titulos += "<th class=\"tb2 \"> Ubicación " + caratulaBajaUbicaciones.getUbicacion()
					+ "</th>";
		}
		titulos += "<th class=\"tb3\"> Totales </th> </tr> </thead><tbody>";
		return titulos;
	}

	String generaInfoTbl(List<CaratulaBajaDatosCaratula> dtsCaratula) {
		String datos = "";
		for (CaratulaBajaDatosCaratula cbdc : dtsCaratula) {
			datos += "<tr ><td class=\"tb1\">" + cbdc.getTitulo() + "</td>";
			float total = 0;
			for (CaratulaBajaUbicaciones ub : cbdc.getUbicaciones()) {
				datos += "<td class=\"tb2 \">" + ub.getPrima() + "</td>";
				String auxPrima = ub.getPrima().replace("$", "").replace(",", "");
				try {
					total += Float.parseFloat(auxPrima);
				} catch (NumberFormatException e) {
					// TODO: handle exception
				}

			}
			DecimalFormat formatter = new DecimalFormat("#,###.##");
			if (total < 0) {

				datos += "<td class=\"tb3\"> -$" + formatter.format((total * (-1))) + "</td></tr>";
			} else {
				datos += "<td class=\"tb3\"> $" + formatter.format(total) + "</td></tr>";
			}
		}
		return datos;
	}
	
	
	
	private boolean isRenovacion(){
		switch (infCotizacion.getModo()) {
			case RENOVACION_AUTOMATICA:
				return true;
			case EDITAR_RENOVACION_AUTOMATICA:
				return true;
			case CONSULTAR_RENOVACION_AUTOMATICA:
				return true;
		
			default:
				return false;
		}
	}
	
	private String infop1Aux(ActionRequest actionRequest){
		try {
			final PortletSession psession = actionRequest.getPortletSession();
			CotizadorDataResponse respuesta = _CMServicesP1.getCotizadorData(infCotizacion.getFolio(),
					infCotizacion.getCotizacion(), infCotizacion.getVersion(),
					user.getScreenName(), infCotizacion.getPantalla());
			int moneda = respuesta.getDatosCotizacion().getMoneda();
			System.out.println("moneda :" + moneda);
			
			InfoAuxPaso1 in1 = new InfoAuxPaso1();
			in1.setMonedaSeleccionada(moneda + "");
			
			
			String nombreDatosGenerales = "LIFERAY_SHARED_F=" + infCotizacion.getFolio() +
					"_C=" + infCotizacion.getCotizacion() +
					"_V=" + infCotizacion.getVersion() +
					"_DATOSP1";
			
			String paso1 = CotizadorModularUtil.objtoJson(in1);
			psession.setAttribute(nombreDatosGenerales, paso1, PortletSession.APPLICATION_SCOPE);
			return paso1;
		} catch (CotizadorModularException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
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

}
