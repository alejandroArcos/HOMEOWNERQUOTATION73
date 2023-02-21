package HomeOwnerQuotation73.commands.resource;

import com.google.gson.Gson;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.pa.cotizadorModularServices.Bean.InfoCotizacion;
import com.tokio.pa.cotizadorModularServices.Bean.ListaDocumentosVoBo492;
import com.tokio.pa.cotizadorModularServices.Exception.CotizadorModularException;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorPaso4;

import java.io.PrintWriter;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import HomeOwnerQuotation73.beans.LogVoBo;
import HomeOwnerQuotation73.constants.HomeOwnerQuotation73PortletKeys;

@Component(immediate = true, property = { 
		"javax.portlet.name=" + HomeOwnerQuotation73PortletKeys.HOMEOWNERQUOTATION73,
		"mvc.command.name=/paso4/getListaDocumentosVoBo492" 
		}, service = MVCResourceCommand.class)

public class GetListaDocumentosVoBo492 extends BaseMVCResourceCommand{
	@Reference
	CotizadorPaso4 _ServicePaso4;
	
	InfoCotizacion infCotizacion = new InfoCotizacion();
	
	@Override
	protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws Exception {
		try {
			User user = (User) resourceRequest.getAttribute(WebKeys.USER);
			String usuario = user.getScreenName();
			int cotizacion = ParamUtil.getInteger(resourceRequest, "cotizacion");
			int version = ParamUtil.getInteger(resourceRequest, "version");
			int pep = ParamUtil.getInteger(resourceRequest, "pep");
			int extranjero = ParamUtil.getInteger(resourceRequest, "extranjero");
			int resideMex = ParamUtil.getInteger(resourceRequest, "resideMex");
			String pantalla = HomeOwnerQuotation73PortletKeys.PANTALLA;
			ListaDocumentosVoBo492 lisDoc = _ServicePaso4.getListaDocumentosVoBo492(cotizacion, version, pep, extranjero, resideMex,  usuario, pantalla);
			Gson gson = new Gson();
			String jsonString = gson.toJson(lisDoc);
			/*
			 * Envio de informacion para quien es quien y log vobo
			 */
			guardaLog(resourceRequest);
			PrintWriter writer = resourceResponse.getWriter();
			writer.write( jsonString );
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			PrintWriter writer = resourceResponse.getWriter();
			writer.write("{\"codigo\" : \"2\", \"error\" : \"sin información\" }");
		}
	}
	
	
	
	private void guardaLog(ResourceRequest resourceRequest){
		String lgVoBo = ParamUtil.getString(resourceRequest, "lgVoBo");
		String listVo = ParamUtil.getString(resourceRequest, "listVo");
		Gson gson = new Gson();
		LogVoBo resultado = gson.fromJson(lgVoBo, LogVoBo.class);
		resultado.setP_tipoVOBO(listVo);
		resultado.setP_resultadoQeQ(HtmlUtil.unescape(resultado.getP_resultadoQeQ()));
		System.out.println("------------------------<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		System.out.println(resultado.toString());
		try {
			_ServicePaso4.registraLogVoBo(resultado.getP_cotizacion(),
					resultado.getP_version(), resultado.getP_usuario(), resultado.getP_idpersona(),
					resultado.getP_tipoVOBO(), resultado.getP_resultadoQeQ(), resultado.getP_estatus());
		} catch (CotizadorModularException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
