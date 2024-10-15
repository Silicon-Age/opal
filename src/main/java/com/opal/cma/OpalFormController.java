package com.opal.cma;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import com.siliconage.web.ControllerServlet;

import com.opal.TransactionContext;

public class OpalFormController extends ControllerServlet {
	private static final org.slf4j.Logger ourLogger = org.slf4j.LoggerFactory.getLogger(OpalFormController.class.getName());
	public static final String ACTION_KEY = "OPAL_FORM_ACTION";
	public static final String SUCCESS_KEY = "OPAL_FORM_SUCCESS";
	
	private static final long serialVersionUID = 1L;
	
	@Override
	protected boolean allowGet() {
		return false;
	}
	
	@Override
	protected String processInternal(HttpServletRequest argRequest, HttpSession argSession, String argUsername) throws Exception {
		String lclUniqueStringParameterName = argRequest.getParameter(OpalForm.FULLY_QUALIFIED_NAME_SEPARATOR + "UniqueStringParameterName");
		
		if (lclUniqueStringParameterName == null) {
			List<Pair<String, String>> lclParameterList = argRequest.getParameterMap().entrySet().stream()
				.map(argEntry -> Pair.of(argEntry.getKey(), Arrays.toString(argEntry.getValue())))
				.collect(Collectors.toList());
			String lclRequestURL = argRequest.getRequestURL().toString();
			
			ourLogger.warn(
				"Missing /UniqueStringParameterName in OpalForm.\n" +
				"IP: " + ObjectUtils.firstNonNull(argRequest.getHeader("X-Forwarded-For"), argRequest.getRemoteAddr()) + '\n' +
				"Referer: " + argRequest.getHeader("referer") + '\n' + 
				"Request: " + lclRequestURL + '\n' +
				"Query string: " + argRequest.getQueryString() + '\n' +
				"Parameter map: " + lclParameterList.toString()
			);
			passBackError("The form you just submitted had an internal technical error.  Please reset the form and try again.  If the problem persists, contact the webmaster.");
			
			return lclRequestURL; // THINK: Is that any good?
		} else {
			OpalFormUpdater<?> lclOFU = OpalFormUpdater.createUpdater(argRequest, "", lclUniqueStringParameterName);
			
			Principal lclPrincipal = argRequest.getUserPrincipal();
			String lclUsername = lclPrincipal == null ? null : StringUtils.trimToNull(lclPrincipal.getName());
			lclOFU.setUsername(lclUsername);
			
			OpalFormAction lclAction = lclOFU.determineAction();
			
			passBack(ACTION_KEY, lclAction);
			
			switch (lclAction) {
				case CANCEL:
					passBack(DO_NOT_PASS_BACK_PRIOR_INPUT_KEY, "TRUE");
					
					// THINK: Does this really make sense?  Is the notion of success just inapplicable for cancellation?
					passBack(SUCCESS_KEY, true);
					
					finishProcess();
					
					return lclOFU.generateCancelURI();
				case DELETE:
					try (TransactionContext lclTC = TransactionContext.createAndActivate(lclOFU.determineDeletionTimeout())) {
						lclOFU.delete();
						if (lclOFU.hasErrors()) {
							lclTC.rollback();
							for (String lclE : lclOFU.getErrors()) {
								passBackError(lclE);
							}
							passBack(SUCCESS_KEY, false);
							return lclOFU.generateFailureURI();
						} else {
							lclTC.complete();
							lclOFU.afterDeleteCommit();
							passBack(DO_NOT_PASS_BACK_PRIOR_INPUT_KEY, "TRUE");
							passBack(SUCCESS_KEY, true);
							finishProcess();
							return lclOFU.generateDeleteURI();
						}
					}
				case SUBMIT:
				case CONTINUE:
					try (TransactionContext lclTC = TransactionContext.createAndActivate(lclOFU.determineUpdateTimeout())) {
						lclOFU.update();
						// ourLogger.info("OpalFormController: just returned from update().  hasErrors == " + lclOFU.hasErrors());
						if (lclOFU.hasErrors()) {
							// ourLogger.info("OpalFormController:  rolling back");
							lclTC.rollback();
							
							for (String lclE : lclOFU.getErrors()) {
								passBackError(lclE);
							}
							// ourLogger.info("Incorrect Field Count: " + lclOFU.getIncorrectFields().size());
							
							String lclLoadTime = StringUtils.trimToNull(argRequest.getParameter(OpalForm.FULLY_QUALIFIED_NAME_SEPARATOR + "LoadTime"));
							
							passBack(ControllerServlet.LOAD_TIME_KEY, lclLoadTime);
							passBack(ControllerServlet.INCORRECT_FIELDS_PASSBACK_KEY, lclOFU.getIncorrectFields());
							passBack(ControllerServlet.CHECKED_NAME_VALUE_PAIRS_PASSBACK_KEY, lclOFU.getCheckedNameValuePairs());
							
							passBack(SUCCESS_KEY, false);
							return lclOFU.generateFailureURI();
						} else {
							// ourLogger.info("OpalFormController:  completing");
							lclTC.complete();
							lclOFU.afterCommit();
							lclOFU.runChildrenAfterCommits();
							
							// Update the update timestamp records for everything
							OpalFormUpdateTimes lclCache = OpalFormUpdateTimes.getInstance();
							Queue<OpalFormUpdater<?>> lclUpdaters = new ArrayDeque<>();
							lclUpdaters.add(lclOFU);
							while (!lclUpdaters.isEmpty()) {
								OpalFormUpdater<?> lclU = lclUpdaters.remove();
								if (lclOFU.getUserFacing() != null && !lclOFU.getUserFacing().isDeleted()) {
									lclCache.setUpdatedNow(lclOFU.getUserFacing());
								}
								lclUpdaters.addAll(lclU.getChildUpdaters());
							}
							
							/* We want to clear the PriorInput if the submission was successful. */
							passBack(DO_NOT_PASS_BACK_PRIOR_INPUT_KEY, "TRUE");
							
							String lclNewURI;
							if (lclAction == OpalFormAction.SUBMIT) {
								lclOFU.successfulSubmit();
								lclNewURI = lclOFU.generateSuccessURI();
							} else {
								Validate.isTrue(lclAction == OpalFormAction.CONTINUE);
								lclOFU.successfulContinue();
								lclNewURI = lclOFU.generateContinueURI();
							}
							finishProcess();
							passBack(SUCCESS_KEY, true);
							return lclNewURI;
						}
					}
				// TODO: Custom actions -- how should they work?
				default:
					throw new IllegalStateException("Unknown action");
			}
		}
	}
}
