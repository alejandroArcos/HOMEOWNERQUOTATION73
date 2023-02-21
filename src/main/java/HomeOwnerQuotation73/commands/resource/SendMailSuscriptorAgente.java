package HomeOwnerQuotation73.commands.resource;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.pa.cotizadorModularServices.Bean.InfoCotizacion;
import com.tokio.pa.cotizadorModularServices.Bean.SlipResponse;
import com.tokio.pa.cotizadorModularServices.Enum.ModoCotizacion;
import com.tokio.pa.cotizadorModularServices.Enum.TipoCotizacion;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorPaso3;
import com.tokio.pa.cotizadorModularServices.Util.CotizadorModularUtil;

import java.io.File;
import java.util.Base64;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.apache.commons.io.FileUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import HomeOwnerQuotation73.constants.HomeOwnerQuotation73PortletKeys;
import HomeOwnerQuotation73.util.SendMailSuscriptorAgenteP3;

@Component(
		immediate = true, property = { 
				"javax.portlet.name=" + HomeOwnerQuotation73PortletKeys.HOMEOWNERQUOTATION73, 
		"mvc.command.name=/sendMailSuscriptorAgente" }, service = MVCResourceCommand.class
		)

public class SendMailSuscriptorAgente extends BaseMVCResourceCommand{
	@Reference
	CotizadorPaso3 _ServicePaso3;
	
	private static final Log _log = LogFactoryUtil.getLog(SendMailSuscriptorAgente.class);
	
	@Override
	protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws Exception {
		
		String folio = ParamUtil.getString(resourceRequest, "folio");
		String cotizacion = ParamUtil.getString(resourceRequest, "cotizacion");
		String version = ParamUtil.getString(resourceRequest, "version");
		String url = ParamUtil.getString(resourceRequest, "url");
		String tipoCotizacion = ParamUtil.getString(resourceRequest, "tipoCotizacion");
		File slip = getSlip(resourceRequest);

//		String	mails = "alejandro.arcos@globalquark.com.mx";
		String	mails = ParamUtil.getString(resourceRequest, "email");
		byte[] decodedBytes = Base64.getDecoder().decode(mails);
		String decodedMails = new String(decodedBytes);
		System.err.println("DECODIFICADO: " + decodedMails);
		String[] listMails = null ;
		if (Validator.isNotNull(decodedMails)){
			listMails = decodedMails.split(",");
		}
		String link = url 
				+ "?infoCotizacion=" + generaUrl(folio, cotizacion, version, ModoCotizacion.EDICION, tipoCotizacion);
		
		String link2 = url 
				+ "?infoCotizacion=" + generaUrl(folio, cotizacion, version, ModoCotizacion.AUX_PASO4, tipoCotizacion);

		_log.info("Url suscriptor -> agente : " + link);
		_log.info("Url directo suscriptor -> agente : " + link2);
		
		System.out.println("url : " + link);
		System.out.println("url2 : " + link2);
		if (Validator.isNotNull(listMails)){
			new SendMailSuscriptorAgenteP3().sendMail(listMails, folio, link, link2, slip);				
		}
	}
	
	File getSlip(ResourceRequest resourceRequest){
		try {
			User user = (User) resourceRequest.getAttribute(WebKeys.USER);
			String usuario = user.getScreenName();
			String cotizacion = ParamUtil.getString(resourceRequest, "cotizacion");
			int version = ParamUtil.getInteger(resourceRequest, "version");
			String pantalla = HomeOwnerQuotation73PortletKeys.HOMEOWNERQUOTATION73;
//			SlipResponse slip = _ServicePaso3.getSlip(cotizacion, version, usuario, pantalla);
			SlipResponse slip = _ServicePaso3.getSlip(cotizacion, version, usuario, pantalla);
			File temp = File.createTempFile(slip.getNombre(), "." + slip.getExtension());
			FileUtils.writeByteArrayToFile(temp, slip.getDocumento());
			return temp;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}	
	}
		
	private String generaUrl(String folio, String cotizacion, String version, ModoCotizacion modo, String tipoCotizacion){
		InfoCotizacion infCot = new InfoCotizacion();
		int intFolio = Integer.parseInt(folio);
		long longCotizacion = Long.parseLong(cotizacion);
		int intVersion = Integer.parseInt(version);
		infCot.setFolio(intFolio);
		infCot.setCotizacion(longCotizacion);
		infCot.setVersion(intVersion);
		infCot.setModo(modo);
		infCot.setPantalla(HomeOwnerQuotation73PortletKeys.HOMEOWNERQUOTATION73);
		infCot.setTipoCotizacion(TipoCotizacion.FAMILIAR);
		return CotizadorModularUtil.encodeURL(infCot);
	}
}
