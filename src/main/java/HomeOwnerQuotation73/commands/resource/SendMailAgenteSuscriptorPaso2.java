package HomeOwnerQuotation73.commands.resource;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.liferay.document.library.kernel.model.DLFolder;
import com.liferay.document.library.kernel.model.DLFolderConstants;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.kernel.service.DLFolderLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.LayoutLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.upload.UploadPortletRequest;
import com.liferay.portal.kernel.util.Base64;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.pa.cotizadorModularServices.Bean.CorreosSuscripcionRequest;
import com.tokio.pa.cotizadorModularServices.Bean.CorreosSuscripcionResponse;
import com.tokio.pa.cotizadorModularServices.Bean.DocumentoResponse;
import com.tokio.pa.cotizadorModularServices.Bean.IdCarpetaResponse;
import com.tokio.pa.cotizadorModularServices.Bean.InfoCotizacion;
import com.tokio.pa.cotizadorModularServices.Constants.CotizadorModularServiceKey;
import com.tokio.pa.cotizadorModularServices.Enum.ModoCotizacion;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorGenerico;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorPaso2;
import com.tokio.pa.cotizadorModularServices.Util.CotizadorModularUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import HomeOwnerQuotation73.constants.HomeOwnerQuotation73PortletKeys;
import HomeOwnerQuotation73.util.SendMail;

/**
 * @author jonathanfviverosmoreno
 *
 */

@Component(immediate = true, property = {
		"javax.portlet.name=" + HomeOwnerQuotation73PortletKeys.HOMEOWNERQUOTATION73,
		"mvc.command.name=/paso2/sendMailAgenteSuscriptor" }, service = MVCResourceCommand.class)

public class SendMailAgenteSuscriptorPaso2 extends BaseMVCResourceCommand {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand#
	 * doServeResource(javax.portlet.ResourceRequest,
	 * javax.portlet.ResourceResponse)
	 */
	
	private static final Log _log = LogFactoryUtil.getLog(SendMailAgenteSuscriptorPaso2.class);

	@Reference
	CotizadorPaso2 _CMServicesP2;
	
	@Reference
	CotizadorGenerico _CMServisGen;
	
	@Reference
	private DLAppService _dlAppService;
	
	InfoCotizacion infCot = null;
	
	
	String errorServicios = "";

	@Override
	protected void doServeResource(ResourceRequest resourceRequest,
			ResourceResponse resourceResponse) throws Exception {
		// TODO Auto-generated method stub
		int isRevire = ParamUtil.getInteger(resourceRequest, "isRevire");
		generaInfoCot(resourceRequest);
		enviaDocuemtos(resourceRequest);
		enviaComentarios(resourceRequest);
		enviaMails(resourceRequest);
		if(isRevire == 1){
			revire(resourceRequest);
		}	
		regresaRespuesta( resourceResponse);
	}
	
	private void generaInfoCot(ResourceRequest resourceRequest){
		Gson gson = new Gson();
		
		String infoCot = ParamUtil.getString(resourceRequest, "infoCot");
		System.out.println("infoCot String :" + infoCot);
		infCot = gson.fromJson(infoCot, InfoCotizacion.class);
		System.out.println("infCot objeto: " + infCot);
		
	}

	private void enviaDocuemtos(ResourceRequest resourceRequest) {
		
		String auxiliarDoc = HtmlUtil.unescape(ParamUtil.getString(resourceRequest, "auxiliarDoc"));
		int totArchivos = ParamUtil.getInteger(resourceRequest, "totArchivos");
		

		User user = (User) resourceRequest.getAttribute(WebKeys.USER);
		String p_usuario = user.getScreenName();

		try {
			if (totArchivos > 0) {

				JSONObject jsonObj;
				jsonObj = JSONFactoryUtil.createJSONObject(auxiliarDoc);
				UploadPortletRequest uploadRequest = PortalUtil
						.getUploadPortletRequest(resourceRequest);

				System.out.println(jsonObj.toString());

				IdCarpetaResponse carpeta = _CMServisGen.SeleccionaIdCarpeta((int)infCot.getFolio(), (int)infCot.getCotizacion(),
						infCot.getVersion());

				for (int i = 0; i < totArchivos; i++) {
					String nombre = "file-" + i;
					File file = uploadRequest.getFile(nombre);
					String mimeType = uploadRequest.getContentType(nombre);

					float n = file.length() / 1024 / 1024;
					float n64 = 4 * (n / 3) + (n % 3 != 0 ? 4 : 0);

					JsonArray listaDocumentos = new JsonArray();
					JsonObject enviaDocumentos = new JsonObject();

					JSONObject jsonObj2;
					jsonObj2 = JSONFactoryUtil.createJSONObject(jsonObj.getString(nombre));

					String nom = jsonObj2.getString("nom").replace(" ", "_");

					enviaDocumentos.addProperty("nombre", nom);
					enviaDocumentos.addProperty("extension", jsonObj2.getString("ext"));
					enviaDocumentos.addProperty("idCarpeta", carpeta.getIdCarpeta());
					enviaDocumentos.addProperty("idDocumento", "0");
					enviaDocumentos.addProperty("idCatalogoDetalle", 1413);

					Map<String, Object> info = null;
					if (n64 > 1.49) {
						info = guardaDocumentos(resourceRequest, file, nom, mimeType,
								jsonObj2.getString("ext"));
						enviaDocumentos.addProperty("documento", "");
						enviaDocumentos.addProperty("url", (String) info.get("url"));
						enviaDocumentos.addProperty("leer", 1);
					} else {
						enviaDocumentos.addProperty("documento",
								Base64.encode(FileUtils.readFileToByteArray(file)));
						enviaDocumentos.addProperty("url", "");
						enviaDocumentos.addProperty("leer", 0);
					}

					System.out.println("json :" + enviaDocumentos.toString());
					listaDocumentos.add(enviaDocumentos);
					try {
						DocumentoResponse respuesta =
								_CMServisGen.wsDocumentos(0, CotizadorModularServiceKey.TMX_CTE_TRANSACCION_POST,
										listaDocumentos, CotizadorModularServiceKey.TMX_CTE_CAT_ACTIVOS,
										"COTIZACION_PA", (int) infCot.getCotizacion(), "", p_usuario, infCot.getPantalla());
								
							
						if (n64 > 1.49) {
							if (respuesta.getMsg().toLowerCase().trim().equals("ok")) {
								elimianArchivo((long) info.get("idDoc"));
							}
						}
					} catch (Exception e) {
						// TODO: handle exception
						agregaErrorString(" - Error al enviar los documentos");
						
						e.getStackTrace();
					}
				}

			}
		} catch (Exception e) {
			// TODO: handle exception
			agregaErrorString(" - Error al cargar los documentos");
			System.out.println("error 2");
			e.printStackTrace();
		}
	}
	
	
	
	Map<String, Object> guardaDocumentos(ResourceRequest resourceRequest, File file, String nombre, String mimeType, String ext){
		try {
			
			Map<String, Object> respuesta = new HashMap<String, Object>();

			long idGroup = PortalUtil.getScopeGroupId(resourceRequest);
			ServiceContext serviceContext = ServiceContextFactory.getInstance(DLFolder.class.getName(), resourceRequest);
			
			serviceContext.setAddGroupPermissions(true);
			serviceContext.setAddGuestPermissions(true);
			
			
			User user = (User) resourceRequest.getAttribute(WebKeys.USER);
			
			String cotizacion = ParamUtil.getString(resourceRequest, "cotizacion");
			String version = ParamUtil.getString(resourceRequest, "version");
			String folio = ParamUtil.getString(resourceRequest, "folio");
			String url = ParamUtil.getString(resourceRequest, "url2");
//			

			
			String aux2 = user.getScreenName() + "-" + nombre + "-F_" + folio + "-C_" + cotizacion + "-V_" + version;
			
			DLFolder fCotizadores = DLFolderLocalServiceUtil.getFolder(idGroup, DLFolderConstants.DEFAULT_PARENT_FOLDER_ID,
					"Documentos_Cotizadores");
			
			
			FileEntry fileEntry = _dlAppService.addFileEntry(idGroup, fCotizadores.getFolderId(), nombre + "." + ext,
					mimeType, nombre + "." + ext, aux2, "hi", file, serviceContext);
			
			
			String urlDoc = url + "/documents/" + idGroup + "/" + fileEntry.getFolderId() + "/" + fileEntry.getFileName()
			+ "/" + fileEntry.getUuid();
			
			System.out.println(urlDoc);
			
			respuesta.put("url", urlDoc); 
			respuesta.put("idDoc", fileEntry.getFileEntryId()); 
			return respuesta;
		} catch (PortalException e) {
			// TODO Auto-generated catch block
			agregaErrorString(" - Error al almacenar los documentos");
			e.printStackTrace();
			return null;
		}
		
	}
	
	void elimianArchivo(long idDoc){
		try {
			_dlAppService.deleteFileEntry(idDoc);
		} catch (PortalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void enviaComentarios(ResourceRequest resourceRequest) {
		try {
			
			String comentario = ParamUtil.getString(resourceRequest, "comentarios");
			comentario = Validator.isNull(comentario) ? "-- Sin comentarios --" : comentario;
			
			
			User user = (User) resourceRequest.getAttribute(WebKeys.USER);
			String usuario = user.getScreenName();
			
			_CMServisGen.guardarComentario(infCot.getCotizacion()+ "", infCot.getVersion(), 1, 0, comentario, usuario, infCot.getPantalla());
			
		} catch (Exception e) {
			// TODO: handle exception
			agregaErrorString(" - Error al enviar los comentarios");
			e.printStackTrace();
		}
		
	}
	
	
	void enviaMails(ResourceRequest resourceRequest){
		
		String comentarios = ParamUtil.getString(resourceRequest, "comentarios");
		
		try {
			String	mails = obtenMails(resourceRequest);
			String[] listMails = null ;
			if (Validator.isNotNull(mails)){
				listMails = mails.split(",");
			}
			
			infCot.setModo(ModoCotizacion.EDICION);
			
			HttpServletRequest originalRequest = PortalUtil
					.getOriginalServletRequest(PortalUtil.getHttpServletRequest(resourceRequest));
			ThemeDisplay themeDisplay = (ThemeDisplay) resourceRequest
					.getAttribute(WebKeys.THEME_DISPLAY);
			String parametro = "?infoCotizacion=" + CotizadorModularUtil.encodeURL(infCot);
			final long GROUP_ID = themeDisplay.getLayout().getGroupId();
			
			String paso = "/" + (infCot.getPantalla().contains("Empresarial") ? "paquete-empresarial" : "paquete-familiar");
			
			Layout layout = LayoutLocalServiceUtil.getFriendlyURLLayout(GROUP_ID, true, paso);
			String urlCotizador = layout.getRegularURL(originalRequest);
			
			String link= urlCotizador + parametro;
			String fol = infCot.getFolio() + "";

			for (String string : listMails) {
				
				_log.info("email : " + string);
			}
			_log.info("Url agente -> suscriptor : " + link);
			System.out.println("url : " + link);
			if (Validator.isNotNull(listMails)){
				String body = "<!DOCTYPE html> <html>   <head>  <meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />  <title>Aviso de folio asignado ${folio}</title>   </head>   <body>     <header><h3>Aviso de folio asignado</h3></header>     <section>       <p>         El folio ${folio} le ha sido asignado para su atención y se le agregaron los siguientes comentarios:       </p>        <p>             ${comentarios}       </p>       <p>         Link directo al folio: ${link}       </p>       <p>Le enviamos un cordial saludo</p>     </section>     <footer>       <img         src='https://preview.ibb.co/i3vFWp/Firma_Correo_Tokio_Marine.png'         alt='Firma Correo Tokio Marine'         width='35%'       />     </footer>   </body> </html> ";
				body = StringUtil.replace(body,
						new String[] { "${folio}", "${comentarios}", "${link}" },
						new String[] { fol, comentarios, link});
				String subject = "Aviso de folio asignado - " + fol;
				new SendMail().SendMAil(listMails, body,  subject);
								
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			agregaErrorString(" - Error al enviar el email");
			e.printStackTrace();
		}
		
	}
	
	String obtenMails(ResourceRequest resourceRequest) {
		try {
			
			HttpServletRequest originalRequest = PortalUtil
					.getOriginalServletRequest(PortalUtil.getHttpServletRequest(resourceRequest));
			
			User user = (User) resourceRequest.getAttribute(WebKeys.USER);
			String p_usuario = user.getScreenName();
			CorreosSuscripcionRequest correo = new CorreosSuscripcionRequest();
			correo.setIdPerfil( (int) originalRequest.getSession().getAttribute("idPerfil"));
			correo.setP_cotizacion( (int) infCot.getCotizacion());
			correo.setP_version(infCot.getVersion());
			correo.setP_pantalla( infCot.getPantalla() );
			correo.setP_usuario(p_usuario);
			
			CorreosSuscripcionResponse respuesta = _CMServicesP2.enviaSuscripcion(correo);
			
			if (respuesta.getCode() == 0){
				if (Validator.isNotNull( respuesta.getEmails())){
					//return "";
					
					return respuesta.getEmails();
					//return "jonfacundov@gmail.com";
				}
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			agregaErrorString(" - Error al recuperar los correos");
		}
		return "";
	}
	
	String generaUrl(ResourceRequest resourceRequest){
		return "";
	}
	
	
	void revire(ResourceRequest resourceRequest){
		
		
		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(WebKeys.THEME_DISPLAY);
		String usuario = themeDisplay.getUser().getScreenName();
		
		String comentario = ParamUtil.getString(resourceRequest, "comentarios");
		comentario = Validator.isNull(comentario) ? "-- Sin comentarios --" : comentario;
		HttpServletRequest originalRequest = PortalUtil
				.getOriginalServletRequest(PortalUtil.getHttpServletRequest(resourceRequest));

		int idPerfilUser = (int) originalRequest.getSession().getAttribute("idPerfil");
		
		try {
			_CMServicesP2.revire(infCot.getCotizacion()+"", infCot.getVersion(), comentario, usuario, infCot.getPantalla(), idPerfilUser);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			agregaErrorString(" - Error al enviar el revire");
			e.printStackTrace();
		}
	}
	
	void regresaRespuesta(ResourceResponse resourceResponse){
		_log.debug("------------> error en: " + errorServicios);
		try {
			PrintWriter writer = resourceResponse.getWriter();
			JsonObject respuesta  = new JsonObject();
			if(Validator.isNull(errorServicios.trim())){
				respuesta.addProperty("code", 0);
				respuesta.addProperty("msj", "ok");
			}else{
				respuesta.addProperty("code", 2);
				
				
				respuesta.addProperty("msj",errorServicios);
			}
			writer.write(respuesta.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	void agregaErrorString(String msgErr){
		if(Validator.isNull(errorServicios)){
			errorServicios = msgErr;
		}else{
			errorServicios += ",<br>" +  errorServicios;
		}
	}

}
