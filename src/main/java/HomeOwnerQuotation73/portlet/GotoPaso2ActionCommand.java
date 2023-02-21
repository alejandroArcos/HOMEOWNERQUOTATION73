/**
 * 
 */
package HomeOwnerQuotation73.portlet;

import com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import org.osgi.service.component.annotations.Component;

import HomeOwnerQuotation73.constants.HomeOwnerQuotation73PortletKeys;

/**
 * @author jonathanfviverosmoreno
 *
 */


@Component(
		immediate = true,
		property = { 
				"javax.portlet.init-param.copy-request-parameters=true",
				"javax.portlet.name=" + HomeOwnerQuotation73PortletKeys.HOMEOWNERQUOTATION73,
				"mvc.command.name=/familiar/paso2"
				},
		service = MVCActionCommand.class
		)
public class GotoPaso2ActionCommand extends BaseMVCActionCommand {

	/* (non-Javadoc)
	 * @see com.liferay.portal.kernel.portlet.bridges.mvc.BaseMVCActionCommand#doProcessAction(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
	 */
	@Override
	protected void doProcessAction(ActionRequest actionRequest, ActionResponse actionResponse)
			throws Exception {
		// TODO Auto-generated method stub
		System.out.println("----->    entreeeeeeeee");
		actionResponse.setRenderParameter("jspPage", "/cotizadorPaso2.jsp");

	}

}
