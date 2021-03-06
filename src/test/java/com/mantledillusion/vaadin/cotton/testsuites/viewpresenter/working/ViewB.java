package com.mantledillusion.vaadin.cotton.testsuites.viewpresenter.working;

import com.mantledillusion.vaadin.cotton.component.ComponentFactory;
import com.mantledillusion.vaadin.cotton.viewpresenter.Presented;
import com.mantledillusion.vaadin.cotton.viewpresenter.View;
import com.vaadin.ui.Component;

@Presented(PresenterB.class)
public class ViewB extends View {

	private static final long serialVersionUID = 1L;
	
	@Override
	protected Component buildUI(TemporalActiveComponentRegistry reg) throws Exception {
		return ComponentFactory.buildButton();
	}
}
