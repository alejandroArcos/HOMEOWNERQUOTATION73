package HomeOwnerQuotation73.commands.action;

import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.cotizador.Bean.Persona;
import com.tokio.pa.cotizadorModularServices.Bean.Cliente;
import com.tokio.pa.cotizadorModularServices.Bean.CotizadorDataResponse;
import com.tokio.pa.cotizadorModularServices.Bean.InfoCotizacion;
import com.tokio.pa.cotizadorModularServices.Bean.InfoP_1_1;
import com.tokio.pa.cotizadorModularServices.Bean.ListaRegistro;
import com.tokio.pa.cotizadorModularServices.Bean.Registro;
import com.tokio.pa.cotizadorModularServices.Bean.SimpleResponse;
import com.tokio.pa.cotizadorModularServices.Bean.VerInfoP1;
import com.tokio.pa.cotizadorModularServices.Constants.CotizadorModularServiceKey;
import com.tokio.pa.cotizadorModularServices.Enum.ModoCotizacion;
import com.tokio.pa.cotizadorModularServices.Enum.TipoCotizacion;
import com.tokio.pa.cotizadorModularServices.Enum.TipoPersona;
import com.tokio.pa.cotizadorModularServices.Exception.CotizadorModularException;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorGenerico;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorPaso1;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorPaso3;
import com.tokio.pa.cotizadorModularServices.Util.CotizadorModularUtil;

import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

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
				"mvc.command.name=/cotizadores/actionPaso1"
				},
		service = MVCActionCommand.class
		)

public class Paso1ActionCommand  extends BaseMVCActionCommand {
	
	@Reference
	CotizadorPaso1 _CMServicesP1;
	
	@Reference
	CotizadorGenerico _CMServicesGenerico;
	
	@Reference
	CotizadorPaso3 _ServicePaso3;
	
	
	InfoCotizacion infCotizacion;
	User user;
	int idPerfilUser;

	@Override
	protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse) throws Exception {
		
		llenaInfoCotizacion(actionRequest);
		CotizadorDataResponse respCotizadorData = getCotizadorData(actionRequest);
		cargaCatalogos(actionRequest, respCotizadorData);
		generaFechas(actionRequest);
		seleccionaModo(actionRequest);

		System.out.println("Do Proccess Action: " + infCotizacion);
		String infoCot = CotizadorModularUtil.objtoJson(infCotizacion);
		System.out.println(infoCot);
		
		getTpoCambio(actionRequest);
		getPermisoVistaP1(actionRequest);

		actionRequest.setAttribute("infCotizacionJson", infoCot);
		actionRequest.setAttribute("inf", infCotizacion);
		actionRequest.setAttribute("perfilSuscriptor", perfilSuscriptor());
		actionRequest.setAttribute("perfilJapones", perfilJapones());
		actionRequest.setAttribute("retroactividad", diasRetroactividad());
		
		actionResponse.setRenderParameter("jspPage", "/cotizadorPaso1.jsp");
	}
	
	private void llenaInfoCotizacion(ActionRequest actionRequest) {

		try {
			HttpServletRequest originalRequest = PortalUtil
					.getOriginalServletRequest(PortalUtil.getHttpServletRequest(actionRequest));

			user = (User) actionRequest.getAttribute(WebKeys.USER);
			idPerfilUser = (int) originalRequest.getSession().getAttribute("idPerfil");

			String inf = actionRequest.getParameter("infoCotizacionBack");
			String legal492 = originalRequest.getParameter("leg492");
			
			System.out.println("Action Back Cot: " + inf);

			String nombreCotizador = "";
			if (Validator.isNotNull(inf)) {
				infCotizacion = CotizadorModularUtil.decodeURL(inf);
				
				System.out.println(infCotizacion);
			} else if (Validator.isNotNull(legal492)) {
				infCotizacion = generaCotLegal(actionRequest);
			} else {

				infCotizacion = new InfoCotizacion();

				infCotizacion.setVersion(1);
				infCotizacion.setTipoCotizacion(TipoCotizacion.FAMILIAR);
			}

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
			actionRequest.setAttribute("perfilMayorEjecutivo", false);
			e.printStackTrace();
		}
	}

	private void cargaCatalogos(ActionRequest actionRequest, CotizadorDataResponse respCotiData) {
		// TODO Auto-generated method stub
		try {

			final PortletSession psession = actionRequest.getPortletSession();
			@SuppressWarnings("unchecked")
			List<Persona> listaAgentes = (List<Persona>) psession.getAttribute("listaAgentes",
					PortletSession.APPLICATION_SCOPE);
			verificaListaAgentes(actionRequest, listaAgentes);

			// caso especial para endosos

			String pantallaEnd = esEndoso() ? "" : infCotizacion.getPantalla();
			
			String codigoAgente = getCodeAgente (respCotiData.getDatosCotizacion().getAgente() , listaAgentes);

			ListaRegistro listaMovimiento = fGetCatalogos(
					CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_MOVIMIENTO,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					pantallaEnd, actionRequest);// (isEndoso ? "" :
												// p_pantalla))

			ListaRegistro listaCatMoneda = fGetCatalogos(
					CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_MONEDA,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					infCotizacion.getPantalla(), actionRequest);
			

			ListaRegistro listaCatFormaPago = fGetCatalogos(
					CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_FORMA_PAGO,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					infCotizacion.getPantalla(), actionRequest);
			

			ListaRegistro listaCatDenominacion = fGetCatalogos(
					CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_DENOMINACION,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					infCotizacion.getPantalla(), actionRequest);
			

			if (infCotizacion.getTipoCotizacion() == TipoCotizacion.EMPRESARIAL) {
				ListaRegistro listaGiros = fGetCatalogos(
						CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
						CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
						CotizadorModularServiceKey.LIST_CAT_GIRO,
						CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
						infCotizacion.getPantalla(), actionRequest);

				actionRequest.setAttribute("listaGiros", listaGiros.getLista());
			}
			
			ListaRegistro listaCatTpoProteccion = fGetCatalogos(
					CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_TIPO_PROTEC,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					infCotizacion.getPantalla(), actionRequest);

			ListaRegistro listaCatGrdoIncendio = fGetCatalogos(
					CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_GRDO_INCEN,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					infCotizacion.getPantalla(), actionRequest);

			ListaRegistro listaCatGrdoRiesRC = fGetCatalogos(
					CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_GRDO_RIES_RC,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					infCotizacion.getPantalla(), actionRequest);

			ListaRegistro listaCatGrdoRiesRCP = fGetCatalogos(
					CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_GRDO_RIES_RCP,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					infCotizacion.getPantalla(), actionRequest);

			ListaRegistro listaCatValorIncen = fGetCatalogos(
					CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_VALOR_IND,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					infCotizacion.getPantalla(), actionRequest);
			
			ListaRegistro listaCatTpoCartera = fGetCatalogos(
					CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_TPO_CAR,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					infCotizacion.getPantalla(), actionRequest);

			/*
			ListaRegistro listaCatCanalNegocio = fGetCatalogos(
					CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_CAN_NEG,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					infCotizacion.getPantalla(), actionRequest);
			*/
			
			ListaRegistro listaCatCanalNegocio = _CMServicesP1.getCanalNegocio(
					CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_CAN_NEG,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS,
					codigoAgente,
					user.getScreenName(),
					infCotizacion.getPantalla());

			ListaRegistro listaCatCoaseguro = fGetCatalogos(
					CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_COASEGURO,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					infCotizacion.getPantalla(), actionRequest);
			
			ListaRegistro listaCatSector = fGetCatalogos(
					CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_SECTOR,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					infCotizacion.getPantalla(), actionRequest);

			actionRequest.setAttribute("listaMovimiento", listaMovimiento.getLista());
			actionRequest.setAttribute("listaCatMoneda", listaCatMoneda.getLista());
			actionRequest.setAttribute("listaAgentes", listaAgentes);
			actionRequest.setAttribute("listaCatDenominacion", listaCatDenominacion.getLista());
			actionRequest.setAttribute("listaCatFormaPago", listaCatFormaPago.getLista());
			
			
			actionRequest.setAttribute("listaCatTpoProteccion", listaCatTpoProteccion.getLista());
			actionRequest.setAttribute("listaCatGrdoIncendio", listaCatGrdoIncendio.getLista());
			actionRequest.setAttribute("listaCatGrdoRiesRC", listaCatGrdoRiesRC.getLista());
			actionRequest.setAttribute("listaCatGrdoRiesRCP", listaCatGrdoRiesRCP.getLista());
			actionRequest.setAttribute("listaCatValorIncen", listaCatValorIncen.getLista());
			actionRequest.setAttribute("listaCatTpoCartera", listaCatTpoCartera.getLista());
			actionRequest.setAttribute("listaCatCanalNegocio", listaCatCanalNegocio.getLista());
			actionRequest.setAttribute("listaCatCoaseguro", listaCatCoaseguro.getLista());
			actionRequest.setAttribute("listaCatSector", listaCatSector.getLista());
			
			

		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("------------------ cargaCatalogos:");
			e.printStackTrace();
		}

	}

	private ListaRegistro fGetCatalogos(int p_rownum, String p_tiptransaccion, String p_codigo,
			int p_activo, String p_usuario, String p_pantalla, ActionRequest actionRequest) {
		try {
			ListaRegistro lr = _CMServicesGenerico.getCatalogo(p_rownum, p_tiptransaccion, p_codigo,
					p_activo, p_usuario, p_pantalla);

			lr.getLista().sort(Comparator.comparing(Registro::getDescripcion));
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

	private void generaFechas(ActionRequest actionRequest) {
		LocalDate fechaHoy = LocalDate.now();
		LocalDate fechaMasAnio = LocalDate.now().plusYears(1);

		actionRequest.setAttribute("fechaHoy", fechaHoy);
		actionRequest.setAttribute("fechaMasAnio", fechaMasAnio);
		actionRequest.setAttribute("perfilMayorEjecutivo", perfilPermisosGeneral());
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

	private void seleccionaModo(ActionRequest actionRequest) {
		CotizadorDataResponse respuesta = new CotizadorDataResponse();
		respuesta.setCode(5);
		respuesta.setMsg("Error al cargar su información");
		try {
			switch (infCotizacion.getModo()) {
				case EDICION:
					respuesta = _CMServicesP1.getCotizadorData(infCotizacion.getFolio(),
							infCotizacion.getCotizacion(), infCotizacion.getVersion(),
							user.getScreenName(), infCotizacion.getPantalla());
					break;
				case COPIA:
					respuesta = _CMServicesP1.copyCotizadorData(infCotizacion.getFolio() + "",
							Integer.parseInt(infCotizacion.getCotizacion() + ""),
							infCotizacion.getVersion(), user.getScreenName(),
							infCotizacion.getPantalla());

					infCotizacion
							.setFolio(Long.parseLong(respuesta.getDatosCotizacion().getFolio()));
					infCotizacion.setCotizacion(respuesta.getDatosCotizacion().getCotizacion());
					infCotizacion.setVersion(respuesta.getDatosCotizacion().getVersion());
					break;
				case ALTA_ENDOSO:
					SimpleResponse infEndo = _CMServicesP1.GuardarCotizacionEndoso(
							infCotizacion.getCotizacion() + "", infCotizacion.getVersion() + "",
							infCotizacion.getPantalla(), user.getScreenName());

					infCotizacion.setFolio(Long.parseLong(infEndo.getFolio()));
					infCotizacion.setCotizacion(infEndo.getCotizacion());
					infCotizacion.setVersion(infEndo.getVersion());

					respuesta = _CMServicesP1.getCotizadorData(infCotizacion.getFolio(),
							infCotizacion.getCotizacion(), infCotizacion.getVersion(),
							user.getScreenName(), infCotizacion.getPantalla());

					actionRequest.setAttribute("perfilMayorEjecutivo", false);
					break;
				case EDITAR_ALTA_ENDOSO:
					respuesta = _CMServicesP1.getCotizadorData(infCotizacion.getFolio(),
							infCotizacion.getCotizacion(), infCotizacion.getVersion(),
							user.getScreenName(), infCotizacion.getPantalla());

					actionRequest.setAttribute("perfilMayorEjecutivo", false);
					break;
				case BAJA_ENDOSO:
					
					SimpleResponse simpleRespuesta = _CMServicesGenerico.guardarCotizacionEndosoBaja(infCotizacion.getCotizacion(),
							infCotizacion.getVersion(), null, 1, 0, 0,
							user.getScreenName() , infCotizacion.getPantalla(), 0, 0);
					
					infCotizacion.setFolio(Long.parseLong(simpleRespuesta.getFolio()));
					infCotizacion.setCotizacion(simpleRespuesta.getCotizacion());
					infCotizacion.setVersion(simpleRespuesta.getVersion());					

					respuesta = _CMServicesP1.getCotizadorData(Long.parseLong(simpleRespuesta.getFolio()),
							simpleRespuesta.getCotizacion(), simpleRespuesta.getVersion(),
							user.getScreenName(), infCotizacion.getPantalla());
					
					actionRequest.setAttribute("perfilMayorEjecutivo", false);
					break;
				case EDITAR_BAJA_ENDOSO:
					respuesta = _CMServicesP1.getCotizadorData(infCotizacion.getFolio(),
							infCotizacion.getCotizacion(), infCotizacion.getVersion(),
							user.getScreenName(), infCotizacion.getPantalla());
					
					infCotizacion.setModo(ModoCotizacion.BAJA_ENDOSO);
					actionRequest.setAttribute("perfilMayorEjecutivo", false);
					break;
				case AUX_PASO4:

					break;
				case NUEVA:
					break;
				case CONSULTA:
					respuesta = _CMServicesP1.getCotizadorData(infCotizacion.getFolio(),
							infCotizacion.getCotizacion(), infCotizacion.getVersion(),
							user.getScreenName(), infCotizacion.getPantalla());
					break;	
				case FACTURA_492 :
					respuesta = _CMServicesP1.getCotizadorData(infCotizacion.getFolio(),
							infCotizacion.getCotizacion(), infCotizacion.getVersion(),
							user.getScreenName(), infCotizacion.getPantalla());
					break;
				case EDICION_JAPONES:
					respuesta = _CMServicesP1.getCotizadorData(infCotizacion.getFolio(),
							infCotizacion.getCotizacion(), infCotizacion.getVersion(),
							user.getScreenName(), infCotizacion.getPantalla());
					break;
				default:
					break;

			}
			
			if(infCotizacion.getModo() != ModoCotizacion.NUEVA) {
				if (respuesta.getDatosCotizacion().getDatosCliente().getTipoPer() == 218) {
					infCotizacion.setTipoPersona(TipoPersona.MORAL);
				} else {
					infCotizacion.setTipoPersona(TipoPersona.FISICA);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		if (infCotizacion.getModo() != ModoCotizacion.NUEVA) {

			if (respuesta.getCode() > 0) {
				SessionErrors.add(actionRequest, "errorConocido");
				actionRequest.setAttribute("errorMsg", respuesta.getMsg());
				SessionMessages.add(actionRequest, PortalUtil.getPortletId(actionRequest)
						+ SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE);
			} else {
				
				Cliente cliente = new Cliente();
				
				cliente.setNombre(respuesta.getDatosCotizacion().getDatosCliente().getNombre());
				cliente.setRfc(respuesta.getDatosCotizacion().getDatosCliente().getRfc());
				cliente.setCodigo(respuesta.getDatosCotizacion().getDatosCliente().getCodigo());
				cliente.setIdPersona(respuesta.getDatosCotizacion().getDatosCliente().getIdPersona());
				
				cliente.setNombre(cliente.getNombre().replace("\"", "\\\""));
				
				respuesta.getDatosCotizacion().getDatosCliente().setNombre(
						respuesta.getDatosCotizacion().getDatosCliente().getNombre()
							.replace("\\", "\\\\\\")
						);
				
				String datosCliente = CotizadorModularUtil
						.objtoJson(respuesta.getDatosCotizacion().getDatosCliente());
				
				
				String datosClienteJSON = CotizadorModularUtil.objtoJson(cliente);

				LocalDate fechaHoy = generaFecha(respuesta.getDatosCotizacion().getFecInicio());
				LocalDate fechaMasAnio = generaFecha(respuesta.getDatosCotizacion().getFecFin());

				if (infCotizacion.getTipoCotizacion().equals(TipoCotizacion.EMPRESARIAL)) {
					getSubgiro(actionRequest, respuesta.getDatosCotizacion().getGiro());
				}

				fechaHoy = validaCambioFecha(fechaHoy);
				
				recuperaPaso1_1(actionRequest);

				actionRequest.setAttribute("fechaHoy", fechaHoy);
				actionRequest.setAttribute("fechaMasAnio", fechaMasAnio);
				actionRequest.setAttribute("cotizadorData", respuesta.getDatosCotizacion());
				actionRequest.setAttribute("datosCliente", datosCliente);
				actionRequest.setAttribute("datosClienteJSON", datosClienteJSON);

			}
		}
	}
	
	
	private void recuperaPaso1_1(ActionRequest actionRequest){
		if (infCotizacion.getTipoCotizacion().equals(TipoCotizacion.EMPRESARIAL)){
			InfoP_1_1 respuesta = null;
			try {
				respuesta = _CMServicesP1.consultaInfoAdicionalPaso1(
						(int) infCotizacion.getCotizacion(), infCotizacion.getVersion(), (int)infCotizacion.getFolio(),
						user.getScreenName(), infCotizacion.getPantalla(), idPerfilUser +"");
				
			} catch (CotizadorModularException e) {
				// TODO Auto-generated catch block
				respuesta = new InfoP_1_1();
				e.printStackTrace();
			}

			actionRequest.setAttribute("P1_1", respuesta.getResultado().get(0));
		}	
	}

	private LocalDate generaFecha(String fecha) {
		String aux = "";
		for (char c : fecha.toCharArray()) {
			aux += Character.isDigit(c) ? c : "";
		}
		Timestamp t = new Timestamp(Long.parseLong(aux));
		return t.toLocalDateTime().toLocalDate();
	}

	private void getSubgiro(ActionRequest actionRequest, int giro) {
		try {
			ListaRegistro catalogo = _CMServicesP1.wsCatalogosDetallePadre(
					CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET, giro,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					infCotizacion.getPantalla());

			catalogo.getLista().sort(Comparator.comparing(Registro::getDescripcion));

			actionRequest.setAttribute("listaSubGiro", catalogo.getLista());
		} catch (CotizadorModularException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private LocalDate fechaMayor(LocalDate fechaOriginal) {
		LocalDate hoy = LocalDate.now();
		if (hoy.isAfter(fechaOriginal)) {
			return hoy;
		}
		return fechaOriginal;
	}

	private LocalDate validaCambioFecha(LocalDate fechaOriginal) {
		switch (infCotizacion.getModo()) {
			case ALTA_ENDOSO:
				return fechaMayor(fechaOriginal);
			case BAJA_ENDOSO:
				return fechaMayor(fechaOriginal);
			case EDITAR_ALTA_ENDOSO:
				return fechaMayor(fechaOriginal);
			case EDITAR_BAJA_ENDOSO:
				return fechaMayor(fechaOriginal);
			default:
				return fechaOriginal;
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

	private void verificaListaAgentes(ActionRequest actionRequest, List<Persona> listaAgentes) {
		if (Validator.isNull(listaAgentes)) {
			SessionErrors.add(actionRequest, "errorConocido");
			actionRequest.setAttribute("errorMsg", "Error al cargar su información cierre sesion");
			SessionMessages.add(actionRequest, PortalUtil.getPortletId(actionRequest)
					+ SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE);
		}
	}
	
	private InfoCotizacion generaCotLegal(ActionRequest actionRequest){
		HttpServletRequest originalRequest = PortalUtil
				.getOriginalServletRequest(PortalUtil.getHttpServletRequest(actionRequest));
		
		InfoCotizacion in = new InfoCotizacion();
		
		String uri = originalRequest.getRequestURI();
		if (uri.toLowerCase().contains("familiar")) {
			in.setTipoCotizacion(TipoCotizacion.FAMILIAR);
			in.setFolio(Long.parseLong(originalRequest.getParameter("folioFamiliar")));
			in.setCotizacion(Long.parseLong(originalRequest.getParameter("cotizacionFamiliar")));
			in.setVersion(Integer.parseInt(originalRequest.getParameter("versionFamiliar")));
		} else if (uri.toLowerCase().contains("empresarial")) {
			in.setTipoCotizacion(TipoCotizacion.EMPRESARIAL);
			in.setFolio(Long.parseLong(originalRequest.getParameter("folioEmpresarial")));
			in.setCotizacion(Long.parseLong(originalRequest.getParameter("cotizacionEmpresarial")));
			in.setVersion(Integer.parseInt(originalRequest.getParameter("versionEmpresarial")));
		} 
		
		in.setModo(ModoCotizacion.FACTURA_492);
		
		System.out.println("-----------");
		System.out.println(in.toString());
		return in;
		
	}
	
	
	boolean esEndoso(){
		switch (infCotizacion.getModo()) {
			case ALTA_ENDOSO:				
				return true;
			case BAJA_ENDOSO:				
				return true;
			case EDITAR_ALTA_ENDOSO:				
				return true;
			case EDITAR_BAJA_ENDOSO:				
				return true;
			default:
				return false;
				
		}
		
	}

	void generaAuxBajaEndoso(long fol, long cot, int ver, ActionRequest actionRequest){
		
		final PortletSession psession = actionRequest.getPortletSession();
		
		
		
		String nombreDatosGenerales = "LIFERAY_SHARED_F=" + infCotizacion.getFolio() +
				"_C=" + infCotizacion.getCotizacion() +
				"_V=" + infCotizacion.getVersion() +
				"_AUXBAJAEND";
		
		SimpleResponse sr = new SimpleResponse();
		sr.setCode(0);
		sr.setCotizacion((int) fol);
		sr.setFolio(cot + "");
		sr.setVersion(ver);
		String auxEnd = CotizadorModularUtil.objtoJson(sr);
		actionRequest.setAttribute("AUXBAJAEND", auxEnd);
		psession.setAttribute(nombreDatosGenerales, auxEnd, PortletSession.APPLICATION_SCOPE);
	}
	
	
	private void getTpoCambio(ActionRequest actionRequest){
		try {
			double tpoCambio =  _ServicePaso3.getTipoCambio().getTipoCambio();
			
			NumberFormat formatoImporte = NumberFormat.getCurrencyInstance();
			String auxFormato = formatoImporte.format(tpoCambio);
			System.out.println("con b: " +  auxFormato);
			
			actionRequest.setAttribute("tpoCambio",  auxFormato);
		} catch (CotizadorModularException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void getPermisoVistaP1(ActionRequest actionRequest){
		int puedeVer = 0;
		try {
			VerInfoP1 permisoVer =_CMServicesP1.mostrarInfoAdicionalPaso1(idPerfilUser+ "", user.getScreenName(), infCotizacion.getPantalla());
			puedeVer = permisoVer.getP_mostrarInfo();
			if(permisoVer.getCode() > 0){
				SessionErrors.add(actionRequest, "errorConocido");
				actionRequest.setAttribute("errorMsg", "Error en permisos ");
				SessionMessages.add(actionRequest, PortalUtil.getPortletId(actionRequest)
						+ SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE);
			}
		} catch (CotizadorModularException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		actionRequest.setAttribute("puedeVerP1", puedeVer);
	}
	
	private int diasRetroactividad() {
		switch (idPerfilUser) {
			case HomeOwnerQuotation73PortletKeys.PERFIL_SUSCRIPTORJR:
				return HomeOwnerQuotation73PortletKeys.DIAS_RETROACTIVOS_SUSCRIPTORJR;
			case HomeOwnerQuotation73PortletKeys.PERFIL_SUSCRIPTORSR:
				return HomeOwnerQuotation73PortletKeys.DIAS_RETROACTIVOS_SUSCRIPTORSR;
			case HomeOwnerQuotation73PortletKeys.PERFIL_SUSCRIPTORMR:
				return HomeOwnerQuotation73PortletKeys.DIAS_RETROACTIVOS_SUSCRIPTORMR;
			case HomeOwnerQuotation73PortletKeys.PERFIL_JAPONES:
				return HomeOwnerQuotation73PortletKeys.DIAS_RETROACTIVOS_SUSCRIPTORJR;
			default: return 0;
		}
	}
	
	private CotizadorDataResponse getCotizadorData(ActionRequest actionRequest){
		CotizadorDataResponse respuesta = new CotizadorDataResponse();
		try {
			respuesta = _CMServicesP1.getCotizadorData(infCotizacion.getFolio(),
					infCotizacion.getCotizacion(), infCotizacion.getVersion(),
					user.getScreenName(), infCotizacion.getPantalla());			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return respuesta;
	}
	
	private String getCodeAgente (int idAgente , List<Persona> listaAgentes){
		String codeAgente = "";
		for (Persona persona : listaAgentes) {
			if(persona.getIdPersona() == idAgente){
				String[] parts = persona.getNombre().split("-");
				codeAgente = parts[0].trim();
				break;
			}
		}
		return codeAgente;
	}

}
