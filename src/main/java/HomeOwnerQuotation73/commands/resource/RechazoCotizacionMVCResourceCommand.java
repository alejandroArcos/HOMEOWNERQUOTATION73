package HomeOwnerQuotation73.commands.resource;

import com.google.gson.Gson;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.WebKeys;
import com.tokio.pa.cotizadorModularServices.Bean.SimpleResponse;
import com.tokio.pa.cotizadorModularServices.Interface.CotizadorPaso3;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import HomeOwnerQuotation73.constants.HomeOwnerQuotation73PortletKeys;

@Component(
		immediate = true, 
		property = { "javax.portlet.name=" + HomeOwnerQuotation73PortletKeys.HOMEOWNERQUOTATION73,
					 "mvc.command.name=/paso3/rechazoCotizacion" },
		service = MVCResourceCommand.class
)

public class RechazoCotizacionMVCResourceCommand extends BaseMVCResourceCommand{
	@Reference
	CotizadorPaso3 _ServicePaso3;
	
	@Override
	protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws Exception {
		String cotizacion = ParamUtil.getString(resourceRequest, "cotizacion");
		int version = ParamUtil.getInteger(resourceRequest, "version");
		int motivoRechazo = ParamUtil.getInteger(resourceRequest, "motivoRechazo");
		String comentario = ParamUtil.getString(resourceRequest, "motivo");
		ThemeDisplay themeDisplay = (ThemeDisplay)resourceRequest.getAttribute(WebKeys.THEME_DISPLAY);
		String usuario = themeDisplay.getUser().getScreenName();
		String pantalla = ParamUtil.getString(resourceRequest, "pantalla");

		HttpServletRequest originalRequest = PortalUtil
				.getOriginalServletRequest(PortalUtil.getHttpServletRequest(resourceRequest));

		int idPerfilUser = (int) originalRequest.getSession().getAttribute("idPerfil");
		
		SimpleResponse simpleResponse = _ServicePaso3.rechazaCotizacion(cotizacion, version, motivoRechazo, comentario, usuario, pantalla, idPerfilUser, 1);
		Gson gson = new Gson();
		String stringJson = gson.toJson(simpleResponse);
		resourceResponse.getWriter().write(stringJson);
	}
}
