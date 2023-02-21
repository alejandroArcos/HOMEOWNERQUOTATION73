package HomeOwnerQuotation73.portlet;

import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.cotizador.Bean.Persona;
import com.tokio.pa.cotizadorModularServices.Bean.Cliente;
import com.tokio.pa.cotizadorModularServices.Bean.CotizadorDataResponse;
import com.tokio.pa.cotizadorModularServices.Bean.InfoCotizacion;
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

import java.io.IOException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import HomeOwnerQuotation73.constants.HomeOwnerQuotation73PortletKeys;

/**
 * @author urielfloresvaldovinos
 */
@Component(
		immediate = true,
		property = {
			"javax.portlet.version=3.0",
			"com.liferay.portlet.display-category=category.sample",
			"com.liferay.portlet.header-portlet-css=/css/main.css",
			"com.liferay.portlet.instanceable=true",
			"javax.portlet.display-name=HOMEOWNERQUOTATION73 Portlet",
			"javax.portlet.init-param.template-path=/",
			"javax.portlet.init-param.view-template=/cotizadorPaso1.jsp",
			"javax.portlet.name=" + HomeOwnerQuotation73PortletKeys.HOMEOWNERQUOTATION73,
			"javax.portlet.resource-bundle=content.Language",
			"javax.portlet.security-role-ref=power-user,user",
			"com.liferay.portlet.private-session-attributes=false",
			"com.liferay.portlet.requires-namespaced-parameters=false",
			"com.liferay.portlet.private-request-attributes=false"
		},
		service = Portlet.class
	)
public class HomeOwnerQuotation73Portlet extends MVCPortlet {
	
	@Reference
	CotizadorPaso1 _CMServicesP1;
	
	@Reference
	CotizadorGenerico _CMServicesGenerico;
	
	@Reference
	CotizadorPaso3 _ServicePaso3;
	
	@Reference
	private DLAppService _dlAppService;
	
	InfoCotizacion infCotizacion;
	User user;
	int idPerfilUser;

	@Override
	public void render(RenderRequest renderRequest, RenderResponse renderResponse)
			throws PortletException, IOException {
		
		//Enumeration<String> en = renderRequest.getParameterNames();
		Set<String> en = renderRequest.getRenderParameters().getNames();
		
		int params = 0;
		
		Iterator<String> it = en.iterator();
		
		while (it.hasNext()) {
			Object objOri = it.next();
			String param = (String) objOri;
			String value = renderRequest.getRenderParameters().getValue(param);
			System.out.println("[ ---> " +param + " : " + value );
			params++;
		}
		
		
		if(params == 0) {
			llenaInfoCotizacion(renderRequest);
			cargaCatalogos(renderRequest);
			generaFechas(renderRequest);
			seleccionaModo(renderRequest, renderResponse);
	
			String infoCot = CotizadorModularUtil.objtoJson(infCotizacion);
			
			getTpoCambio(renderRequest);
			getPermisoVistaP1(renderRequest);
			
			urlDocCargaMasiva(renderRequest);
			
			renderRequest.setAttribute("infCotizacionJson", infoCot);
			renderRequest.setAttribute("inf", infCotizacion);
			renderRequest.setAttribute("perfilSuscriptor", perfilSuscriptor());
			renderRequest.setAttribute("perfilJapones", perfilJapones());
			renderRequest.setAttribute("retroactividad", diasRetroactividad());
		}
		

		super.render(renderRequest, renderResponse);
	}

	private void llenaInfoCotizacion(RenderRequest renderRequest) {

		try {
			HttpServletRequest originalRequest = PortalUtil
					.getOriginalServletRequest(PortalUtil.getHttpServletRequest(renderRequest));

			user = (User) renderRequest.getAttribute(WebKeys.USER);
			idPerfilUser = (int) originalRequest.getSession().getAttribute("idPerfil");

			String inf = originalRequest.getParameter("infoCotizacion");
			String legal492 = originalRequest.getParameter("leg492");

			String nombreCotizador = "";
			if (Validator.isNotNull(inf)) {
				infCotizacion = CotizadorModularUtil.decodeURL(inf);
			} else if (Validator.isNotNull(legal492)) {
				infCotizacion = generaCotLegal(renderRequest);
			} else {

				infCotizacion = new InfoCotizacion();

				infCotizacion.setVersion(1);
				infCotizacion.setTipoCotizacion(TipoCotizacion.FAMILIAR);
			}
			
			infCotizacion.setPantalla(HomeOwnerQuotation73PortletKeys.PANTALLA_FAMILIAR);
			nombreCotizador = HomeOwnerQuotation73PortletKeys.TITULO_FAMILIAR;
			renderRequest.setAttribute("tituloCotizador", nombreCotizador);
			renderRequest.setAttribute("idPerfilUser", idPerfilUser);
		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("------------------ llenaInfoCotizacion:");
			renderRequest.setAttribute("perfilMayorEjecutivo", false);
			e.printStackTrace();
		}
	}

	private void cargaCatalogos(RenderRequest renderRequest) {
		// TODO Auto-generated method stub
		try {

			final PortletSession psession = renderRequest.getPortletSession();
			@SuppressWarnings("unchecked")
			List<Persona> listaAgentes = (List<Persona>) psession.getAttribute("listaAgentes",
					PortletSession.APPLICATION_SCOPE);
			verificaListaAgentes(renderRequest, listaAgentes);

			// caso especial para endosos

			String pantallaEnd = esEndoso() ? "" : infCotizacion.getPantalla();
			
			

			ListaRegistro listaMovimiento = fGetCatalogos(
					CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_MOVIMIENTO,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					pantallaEnd, renderRequest);

			ListaRegistro listaCatMoneda = fGetCatalogos(
					CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_MONEDA,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					infCotizacion.getPantalla(), renderRequest);

			ListaRegistro listaCatFormaPago = fGetCatalogos(
					CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_FORMA_PAGO,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					infCotizacion.getPantalla(), renderRequest);

			ListaRegistro listaCatDenominacion = fGetCatalogos(
					CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_DENOMINACION,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					infCotizacion.getPantalla(), renderRequest);


			ListaRegistro listaCatCoaseguro = fGetCatalogos(
					CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_COASEGURO,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					infCotizacion.getPantalla(), renderRequest);
			
			ListaRegistro listaCatSector = fGetCatalogos(
					CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
					CotizadorModularServiceKey.LIST_CAT_SECTOR,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					infCotizacion.getPantalla(), renderRequest);

			renderRequest.setAttribute("listaMovimiento", listaMovimiento.getLista());
			renderRequest.setAttribute("listaCatMoneda", listaCatMoneda.getLista());
			renderRequest.setAttribute("listaAgentes", listaAgentes);
			renderRequest.setAttribute("listaCatDenominacion", listaCatDenominacion.getLista());
			renderRequest.setAttribute("listaCatFormaPago", listaCatFormaPago.getLista());
			renderRequest.setAttribute("listaCatCoaseguro", listaCatCoaseguro.getLista());
			renderRequest.setAttribute("listaCatSector", listaCatSector.getLista());
			
			

		} catch (Exception e) {
			// TODO: handle exception
			System.err.println("------------------ cargaCatalogos:");
			e.printStackTrace();
		}

	}

	private ListaRegistro fGetCatalogos(int p_rownum, String p_tiptransaccion, String p_codigo,
			int p_activo, String p_usuario, String p_pantalla, RenderRequest renderRequest) {
		try {
			ListaRegistro lr = _CMServicesGenerico.getCatalogo(p_rownum, p_tiptransaccion, p_codigo,
					p_activo, p_usuario, p_pantalla);

			lr.getLista().sort(Comparator.comparing(Registro::getDescripcion));
			return lr;
		} catch (Exception e) {
			System.err.print("----------------- error en traer los catalogos");
			e.printStackTrace();
			SessionErrors.add(renderRequest, "errorConocido");
			renderRequest.setAttribute("errorMsg", "Error en catalogos");
			SessionMessages.add(renderRequest, PortalUtil.getPortletId(renderRequest)
					+ SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE);
			return null;
		}
	}

	private void generaFechas(RenderRequest renderRequest) {
		LocalDate fechaHoy = LocalDate.now();
		LocalDate fechaMasAnio = LocalDate.now().plusYears(1);

		renderRequest.setAttribute("fechaHoy", fechaHoy);
		renderRequest.setAttribute("fechaMasAnio", fechaMasAnio);
		renderRequest.setAttribute("perfilMayorEjecutivo", perfilPermisosGeneral());
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

	private void seleccionaModo(RenderRequest renderRequest, RenderResponse renderResponse) {
		CotizadorDataResponse respuesta = new CotizadorDataResponse();
		respuesta.setCode(5);
		respuesta.setMsg("Error al cargar su información");
		
		try {
			
			final PortletSession psession = renderRequest.getPortletSession();
			@SuppressWarnings("unchecked")
			List<Persona> listaAgentes = (List<Persona>) psession.getAttribute("listaAgentes",
					PortletSession.APPLICATION_SCOPE);
			verificaListaAgentes(renderRequest, listaAgentes);
			
			String codigoAgente = "";
			
			ListaRegistro listaCatCanalNegocio = null;
			
			switch (infCotizacion.getModo()) {
				case EDICION:
					respuesta = _CMServicesP1.getCotizadorData(infCotizacion.getFolio(),
							infCotizacion.getCotizacion(), infCotizacion.getVersion(),
							user.getScreenName(), infCotizacion.getPantalla());
					
					codigoAgente = getCodeAgente (respuesta.getDatosCotizacion().getAgente() , listaAgentes);
					
					listaCatCanalNegocio = _CMServicesP1.getCanalNegocio(
							CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
							CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
							CotizadorModularServiceKey.LIST_CAT_CAN_NEG,
							CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS,
							codigoAgente,
							user.getScreenName(),
							infCotizacion.getPantalla());
					
					renderRequest.setAttribute("listaCatCanalNegocio", listaCatCanalNegocio.getLista());
					
					
					validaFolioUsuario((int)infCotizacion.getCotizacion(),
							infCotizacion.getVersion(), idPerfilUser, user.getScreenName(),
							infCotizacion.getPantalla(), renderResponse);
					
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
					
					
					validaFolioUsuario((int)infCotizacion.getCotizacion(),
							infCotizacion.getVersion(), idPerfilUser, user.getScreenName(),
							infCotizacion.getPantalla(), renderResponse);
					
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

					renderRequest.setAttribute("perfilMayorEjecutivo", false);
					
					validaFolioUsuario((int)infCotizacion.getCotizacion(),
							infCotizacion.getVersion(), idPerfilUser, user.getScreenName(),
							infCotizacion.getPantalla(), renderResponse);
					
					break;
				case EDITAR_ALTA_ENDOSO:
					respuesta = _CMServicesP1.getCotizadorData(infCotizacion.getFolio(),
							infCotizacion.getCotizacion(), infCotizacion.getVersion(),
							user.getScreenName(), infCotizacion.getPantalla());

					renderRequest.setAttribute("perfilMayorEjecutivo", false);
					
					validaFolioUsuario((int)infCotizacion.getCotizacion(),
							infCotizacion.getVersion(), idPerfilUser, user.getScreenName(),
							infCotizacion.getPantalla(), renderResponse);
					
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
					
					renderRequest.setAttribute("perfilMayorEjecutivo", false);
					
					validaFolioUsuario((int)infCotizacion.getCotizacion(),
							infCotizacion.getVersion(), idPerfilUser, user.getScreenName(),
							infCotizacion.getPantalla(), renderResponse);
					
					break;
				case EDITAR_BAJA_ENDOSO:
					respuesta = _CMServicesP1.getCotizadorData(infCotizacion.getFolio(),
							infCotizacion.getCotizacion(), infCotizacion.getVersion(),
							user.getScreenName(), infCotizacion.getPantalla());
					
					infCotizacion.setModo(ModoCotizacion.BAJA_ENDOSO);
					renderRequest.setAttribute("perfilMayorEjecutivo", false);
					
					validaFolioUsuario((int)infCotizacion.getCotizacion(),
							infCotizacion.getVersion(), idPerfilUser, user.getScreenName(),
							infCotizacion.getPantalla(), renderResponse);
					
					break;
				case AUX_PASO4:

					break;
				case NUEVA:
					
					HttpServletRequest originalRequest = PortalUtil
						.getOriginalServletRequest(PortalUtil.getHttpServletRequest(renderRequest));
					
					String btoa = originalRequest.getParameter("btoa");
					
					if(Validator.isNotNull(btoa)) {
					
						byte[] decodedBytes = Base64.getUrlDecoder().decode(btoa);		
						String decodeb64 = new String(decodedBytes);
						String idSolicitud = decodeb64.split(";")[0];
						
						infCotizacion.setSolicitud(idSolicitud);
						
						renderRequest.setAttribute("numeroSolicitud", true);
					}
					
					break;
				case CONSULTA:
					respuesta = _CMServicesP1.getCotizadorData(infCotizacion.getFolio(),
							infCotizacion.getCotizacion(), infCotizacion.getVersion(),
							user.getScreenName(), infCotizacion.getPantalla());
					
					codigoAgente = getCodeAgente (respuesta.getDatosCotizacion().getAgente() , listaAgentes);
					
					listaCatCanalNegocio = _CMServicesP1.getCanalNegocio(
							CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
							CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
							CotizadorModularServiceKey.LIST_CAT_CAN_NEG,
							CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS,
							codigoAgente,
							user.getScreenName(),
							infCotizacion.getPantalla());
					
					renderRequest.setAttribute("listaCatCanalNegocio", listaCatCanalNegocio.getLista());
					
					
					validaFolioUsuario((int)infCotizacion.getCotizacion(),
							infCotizacion.getVersion(), idPerfilUser, user.getScreenName(),
							infCotizacion.getPantalla(), renderResponse);
					
					break;	
				case FACTURA_492 :
					respuesta = _CMServicesP1.getCotizadorData(infCotizacion.getFolio(),
							infCotizacion.getCotizacion(), infCotizacion.getVersion(),
							user.getScreenName(), infCotizacion.getPantalla());
					
					codigoAgente = getCodeAgente (respuesta.getDatosCotizacion().getAgente() , listaAgentes);
					
					listaCatCanalNegocio = _CMServicesP1.getCanalNegocio(
							CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
							CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
							CotizadorModularServiceKey.LIST_CAT_CAN_NEG,
							CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS,
							codigoAgente,
							user.getScreenName(),
							infCotizacion.getPantalla());
					
					renderRequest.setAttribute("listaCatCanalNegocio", listaCatCanalNegocio.getLista());
					
					validaFolioUsuario((int)infCotizacion.getCotizacion(),
							infCotizacion.getVersion(), idPerfilUser, user.getScreenName(),
							infCotizacion.getPantalla(), renderResponse);
					
					break;
				case EDICION_JAPONES:
					respuesta = _CMServicesP1.getCotizadorData(infCotizacion.getFolio(),
							infCotizacion.getCotizacion(), infCotizacion.getVersion(),
							user.getScreenName(), infCotizacion.getPantalla());
					
					codigoAgente = getCodeAgente (respuesta.getDatosCotizacion().getAgente() , listaAgentes);
					
					listaCatCanalNegocio = _CMServicesP1.getCanalNegocio(
							CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
							CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET,
							CotizadorModularServiceKey.LIST_CAT_CAN_NEG,
							CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS,
							codigoAgente,
							user.getScreenName(),
							infCotizacion.getPantalla());
					
					renderRequest.setAttribute("listaCatCanalNegocio", listaCatCanalNegocio.getLista());
					
					validaFolioUsuario((int)infCotizacion.getCotizacion(),
							infCotizacion.getVersion(), idPerfilUser, user.getScreenName(),
							infCotizacion.getPantalla(), renderResponse);
					
					break;
				case CONSULTAR_REVISION:
					respuesta = _CMServicesP1.getCotizadorData(infCotizacion.getFolio(),
							infCotizacion.getCotizacion(), infCotizacion.getVersion(),
							user.getScreenName(), infCotizacion.getPantalla());
					
					validaFolioUsuario((int)infCotizacion.getCotizacion(),
							infCotizacion.getVersion(), idPerfilUser, user.getScreenName(),
							infCotizacion.getPantalla(), renderResponse);
					
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
			e.printStackTrace();
		}

		if (infCotizacion.getModo() != ModoCotizacion.NUEVA) {

			if (respuesta.getCode() > 0) {
				SessionErrors.add(renderRequest, "errorConocido");
				renderRequest.setAttribute("errorMsg", respuesta.getMsg());
				SessionMessages.add(renderRequest, PortalUtil.getPortletId(renderRequest)
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
				
				String datosCliente = CotizadorModularUtil.objtoJson(respuesta.getDatosCotizacion()
						.getDatosCliente());
				
				String datosClienteJSON = CotizadorModularUtil.objtoJson(cliente);
				
				System.out.println(datosCliente);
				System.out.println(datosClienteJSON);

				LocalDate fechaHoy = generaFecha(respuesta.getDatosCotizacion().getFecInicio());
				LocalDate fechaMasAnio = generaFecha(respuesta.getDatosCotizacion().getFecFin());

				if (infCotizacion.getTipoCotizacion().equals(TipoCotizacion.EMPRESARIAL)) {
					getSubgiro(renderRequest, respuesta.getDatosCotizacion().getGiro());
				}

				fechaHoy = validaCambioFecha(fechaHoy);


				renderRequest.setAttribute("fechaHoy", fechaHoy);
				renderRequest.setAttribute("fechaMasAnio", fechaMasAnio);
				renderRequest.setAttribute("cotizadorData", respuesta.getDatosCotizacion());
				renderRequest.setAttribute("datosCliente", datosCliente);
				renderRequest.setAttribute("datosClienteJSON", datosClienteJSON);
				

			}
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

	private void getSubgiro(RenderRequest renderRequest, int giro) {
		try {
			ListaRegistro catalogo = _CMServicesP1.wsCatalogosDetallePadre(
					CotizadorModularServiceKey.TMX_CTE_ROW_TODOS,
					CotizadorModularServiceKey.TMX_CTE_TRANSACCION_GET, giro,
					CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS, user.getScreenName(),
					infCotizacion.getPantalla());

			catalogo.getLista().sort(Comparator.comparing(Registro::getDescripcion));

			renderRequest.setAttribute("listaSubGiro", catalogo.getLista());
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

	private int perfilSuscriptor() {
		try {
			switch (idPerfilUser) {
				case HomeOwnerQuotation73PortletKeys.PERFIL_SUSCRIPTORJR:
					return 1;
				case HomeOwnerQuotation73PortletKeys.PERFIL_SUSCRIPTORSR:
					return 1;
				case HomeOwnerQuotation73PortletKeys.PERFIL_SUSCRIPTORMR:
					return 1;
				case HomeOwnerQuotation73PortletKeys.PERFIL_JAPONES:
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

	private void verificaListaAgentes(RenderRequest renderRequest, List<Persona> listaAgentes) {
		if (Validator.isNull(listaAgentes)) {
			SessionErrors.add(renderRequest, "errorConocido");
			renderRequest.setAttribute("errorMsg", "Error al cargar su información cierre sesion");
			SessionMessages.add(renderRequest, PortalUtil.getPortletId(renderRequest)
					+ SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE);
		}
	}
	
	private InfoCotizacion generaCotLegal(RenderRequest renderRequest){
		HttpServletRequest originalRequest = PortalUtil
				.getOriginalServletRequest(PortalUtil.getHttpServletRequest(renderRequest));
		
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

	void generaAuxBajaEndoso(long fol, long cot, int ver, RenderRequest renderRequest){
		
		final PortletSession psession = renderRequest.getPortletSession();
		
		
		
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
		renderRequest.setAttribute("AUXBAJAEND", auxEnd);
		psession.setAttribute(nombreDatosGenerales, auxEnd, PortletSession.APPLICATION_SCOPE);
	}
	
	
	private void getTpoCambio(RenderRequest renderRequest){
		try {
			double tpoCambio =  _ServicePaso3.getTipoCambio().getTipoCambio();
			
			NumberFormat formatoImporte = NumberFormat.getCurrencyInstance();
			String auxFormato = formatoImporte.format(tpoCambio);
			System.out.println("con b: " +  auxFormato);
			
			renderRequest.setAttribute("tpoCambio",  auxFormato);
		} catch (CotizadorModularException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void getPermisoVistaP1(RenderRequest renderRequest){
		int puedeVer = 0;
		try {
			VerInfoP1 permisoVer =_CMServicesP1.mostrarInfoAdicionalPaso1(idPerfilUser+ "", user.getScreenName(), infCotizacion.getPantalla());
			puedeVer = permisoVer.getP_mostrarInfo();
			if(permisoVer.getCode() > 0){
				SessionErrors.add(renderRequest, "errorConocido");
				renderRequest.setAttribute("errorMsg", "Error en permisos ");
				SessionMessages.add(renderRequest, PortalUtil.getPortletId(renderRequest)
						+ SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE);
			}
		} catch (CotizadorModularException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		renderRequest.setAttribute("puedeVerP1", puedeVer);
	}
	
	private CotizadorDataResponse getCotizadorData(RenderRequest actionRequest){
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
	
	private void validaFolioUsuario(int cotizacion, int version, int perfilId, String usuario, String pantalla, RenderResponse renderResponse) throws IOException{
		SimpleResponse resp = new SimpleResponse();
		try {
			resp = _CMServicesGenerico.validaFolioUsuario(cotizacion, version, perfilId, usuario, pantalla);
		} catch (Exception e) {
			// TODO: handle exception	
			System.err.println("Error al validar permisos por perfil");
			resp.setCode(1);
		}finally {
			if( resp.getCode() != 0 ){
				PortalUtil.getHttpServletResponse(renderResponse).sendRedirect("/group/portal-agentes/" );
			}						
		}
	}
	
	private void urlDocCargaMasiva(RenderRequest renderRequest){
		
		try {
			long idGroup = PortalUtil.getScopeGroupId(renderRequest);
			DLFolder fCotizadores = DLFolderLocalServiceUtil.getFolder(idGroup, DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
					"Documentos_Aux_Cotizadores");
			FileEntry fileEntry = _dlAppService.getFileEntry(idGroup, fCotizadores.getFolderId(), "FAM_INFO_V02.xlsm");
			String urlDoc = renderRequest.getScheme() + "://" + renderRequest.getServerName() + ":" + renderRequest.getServerPort() + "/documents/" + idGroup + "/" + fileEntry.getFolderId() + "/" + fileEntry.getFileName()
			+ "/" + fileEntry.getUuid();
			System.out.println("-----> url : " + urlDoc);
			
			String url = renderRequest.getScheme() + "://" + renderRequest.getServerName() + ":" + renderRequest.getServerPort() + renderRequest.getContextPath();
			System.out.println(url);
			renderRequest.setAttribute("urlDoc", urlDoc);
		} catch (PortalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private int perfilSuscriptorJr() {
		try {
			switch (idPerfilUser) {
				case HomeOwnerQuotation73PortletKeys.PERFIL_SUSCRIPTORJR:
					return 1;
			}
			return 0;
		} catch (Exception e) {
			// TODO: handle exception
			return 0;
		}
	}
	
	private int perfilSuscriptorSrMr() {
		try {
			switch (idPerfilUser) {
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
	
	private boolean perfilSuscriptorGeneral() {
		try {
			switch (idPerfilUser) {
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
	
	
	private int perfilAgenteEjecutivo() {
		try {
			switch (idPerfilUser) {
				case HomeOwnerQuotation73PortletKeys.PERFIL_AGENTE:
					return 1;
				case HomeOwnerQuotation73PortletKeys.PERFIL_EJECUTIVO:
					return 1;
			}
			return 0;
		} catch (Exception e) {
			// TODO: handle exception
			return 0;
		}
	}
	
	private int perfilAgente() {
		try {
			switch (idPerfilUser) {
				case HomeOwnerQuotation73PortletKeys.PERFIL_AGENTE:
					return 1;
			}
			return 0;
		} catch (Exception e) {
			// TODO: handle exception
			return 0;
		}
	}
	
	private int perfilEjecutivo() {
		try {
			switch (idPerfilUser) {
				case HomeOwnerQuotation73PortletKeys.PERFIL_EJECUTIVO:
					return 1;
			}
			return 0;
		} catch (Exception e) {
			// TODO: handle exception
			return 0;
		}
	}
	
	private void viewOnlySubcriptor( RenderResponse renderResponse ){
		if( !perfilSuscriptorGeneral() && (perfilJapones() != 1) && (perfilEjecutivo() != 1)) {
			String location = "/group/portal-agentes/";
			try {
				PortalUtil.getHttpServletResponse(renderResponse).sendRedirect(location );
				
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("Error al redireccionar");
			}
		}
	}
}