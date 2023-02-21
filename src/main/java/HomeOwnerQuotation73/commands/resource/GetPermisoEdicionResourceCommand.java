package HomeOwnerQuotation73.commands.resource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCResourceCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCResourceCommand;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.util.WebKeys;

import java.io.PrintWriter;
import java.util.List;

import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.osgi.service.component.annotations.Component;

import HomeOwnerQuotation73.constants.HomeOwnerQuotation73PortletKeys;

@Component(
	property = {
		"javax.portlet.name=" + HomeOwnerQuotation73PortletKeys.HOMEOWNERQUOTATION73,
		"mvc.command.name=/cotizadores/paso3/getPermisoEmision"
	},
	service = MVCResourceCommand.class
)

public class GetPermisoEdicionResourceCommand extends BaseMVCResourceCommand {
	
	private static final Log _log = LogFactoryUtil.getLog(GetPermisoEdicionResourceCommand.class);

	@Override
	protected void doServeResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse)
			throws Exception {
		
		User user = (User) resourceRequest.getAttribute(WebKeys.USER);
		
		List<Role> userRoles = RoleLocalServiceUtil.getUserRoles(user.getUserId());
		
		JsonObject response = new JsonObject();
		Gson gson = new Gson();
		
		boolean emision = false;
		
		for(Role roleAux : userRoles) {
			
			_log.info(roleAux.getName());
			
			if(roleAux.getName().equals("TMX-EMISION HOMEOWNER")) {
				emision = true;
			}
		}
		
		if(emision) {
			response.addProperty("code", 0);
		}
		else {
			response.addProperty("code", 2);
		}
		
		PrintWriter writer = resourceResponse.getWriter();
		String responseString = gson.toJson(response);
		writer.write(responseString);
	}
	
	
}
