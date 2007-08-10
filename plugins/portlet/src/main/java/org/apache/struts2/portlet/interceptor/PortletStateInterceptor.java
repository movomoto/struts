package org.apache.struts2.portlet.interceptor;

import java.util.Map;

import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.portlet.PortletActionConstants;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.opensymphony.xwork2.util.CompoundRoot;
import com.opensymphony.xwork2.util.TextUtils;
import com.opensymphony.xwork2.util.ValueStack;

public class PortletStateInterceptor extends AbstractInterceptor implements PortletActionConstants {

	private final static Log LOG = LogFactory.getLog(PortletStateInterceptor.class);

	private static final long serialVersionUID = 6138452063353911784L;

	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		Integer phase = (Integer) invocation.getInvocationContext().get(PHASE);
		if (RENDER_PHASE.equals(phase)) {
			restoreStack(invocation);
			return invocation.invoke();
		} else if (EVENT_PHASE.equals(phase)) {
			try {
				return invocation.invoke();
			} finally {
				saveStack(invocation);
			}
		} else {
			return invocation.invoke();
		}
	}

	private void saveStack(ActionInvocation invocation) {
		Map session = invocation.getInvocationContext().getSession();
		session.put("struts.portlet.valueStackFromEventPhase", invocation.getStack());
		ActionResponse actionResponse = (ActionResponse) invocation.getInvocationContext().get(RESPONSE);
		actionResponse.setRenderParameter(EVENT_ACTION, "true");
	}

	private void restoreStack(ActionInvocation invocation) {
		RenderRequest request = (RenderRequest) invocation.getInvocationContext().get(REQUEST);
		if (TextUtils.stringSet(request.getParameter(EVENT_ACTION))) {
			LOG.debug("Restoring value stack from event phase");
			ValueStack oldStack = (ValueStack) invocation.getInvocationContext().getSession().get(
					"struts.portlet.valueStackFromEventPhase");
			if (oldStack != null) {
				CompoundRoot oldRoot = oldStack.getRoot();
				ValueStack currentStack = invocation.getStack();
				CompoundRoot root = currentStack.getRoot();
				root.addAll(oldRoot);
				LOG.debug("Restored stack");
			}
		}
	}

}
