package com.ceteva.diagram.palette;

import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartListener;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editpolicies.GraphicalEditPolicy;
import org.eclipse.swt.widgets.Display;

public abstract class DiagramAssistantEditPolicy extends GraphicalEditPolicy
		implements MouseMotionListener {

	/**
	 * The <code>Runnable</code> used when a timer is started to show the
	 * diagram assistant after a certain amount of time has passed.
	 */
	private class ShowDiagramAssistantRunnable implements Runnable {

		/** the mouse location when the timer was started */
		private Point originalMouseLocation;

		/**
		 * Creates a new instance.
		 * 
		 * @param originalMouseLocation
		 *            the current mouse location
		 */
		protected ShowDiagramAssistantRunnable(Point originalMouseLocation) {
			this.originalMouseLocation = originalMouseLocation;
		}

		/**
		 * The diagram assistant added when this task is run if the mouse is
		 * still at the same spot where it was when the timer was started (i.e.
		 * only add the diagram assistant when the user stops moving the mouse).
		 */
		public void run() {
			if (originalMouseLocation != null
					&& originalMouseLocation.equals(getMouseLocation())) {
				if (isDiagramAssistantShowing()
						&& !shouldAvoidHidingDiagramAssistant()) {
					hideDiagramAssistant();
				}
				if (shouldShowDiagramAssistant()) {
					showDiagramAssistant(originalMouseLocation);
				}
			}
		}
	}

	/**
	 * The <code>Runnable</code> used when a timer is started to hide the
	 * diagram assistant after a certain amount of time has passed.
	 */
	private Runnable hideDiagramAssistantRunnable = new Runnable() {

		/**
		 * The diagram assistant is removed when this task is run if the mouse
		 * is still outside the shape.
		 */
		public void run() {
			if (getMouseLocation() == null
					|| !shouldAvoidHidingDiagramAssistant()) {
				hideDiagramAssistant();
			}
		}
	};

	/**
	 * Listens to the focus events on the owner editpart so that the diagram
	 * assistant can be added when the space bar is pressed. I tried to use
	 * IFigure.addFocusListener(), but the figure isn't getting any focus change
	 * events when the space bar is pressed.
	 */
	private class FocusListener extends EditPartListener.Stub {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.gef.EditPartListener#selectedStateChanged(org.eclipse.gef.EditPart)
		 */
		public void selectedStateChanged(EditPart part) {
			if (part.hasFocus() && shouldShowDiagramAssistant()) {
				showDiagramAssistant(getMouseLocation());
			} else {
				hideDiagramAssistant();
			}
		}
	}

	/**
	 * The amount of time to wait before showing the diagram assistant.
	 */
	private static final int APPEARANCE_DELAY = 200;

	/**
	 * The amount of time to wait before hiding the diagram assistant after it
	 * has been made visible.
	 */
	private static final int DISAPPEARANCE_DELAY = 2000;

	/**
	 * The amount of time to wait before hiding the diagram assistant after the
	 * user has moved the mouse outside of the editpart.
	 */
	private static final int DISAPPEARANCE_DELAY_UPON_EXIT = 1000;

	/**
	 * The current mouse location within the host used to determine where the
	 * diagram assistant should be displayed. This will be null if the mouse is
	 * outside the host and diagram assistant figure.
	 */
	private Point mouseLocation;

	/** Listens to focus change events on the host editpart. */
	private FocusListener focusListener = new FocusListener();

	/** Flag to indicate that the diagram assistant should not be hidden. */
	private boolean avoidHidingDiagramAssistant = true;

	/**
	 * Creates a new instance.
	 */
	public DiagramAssistantEditPolicy() {
		super();
	}

	/**
	 * Checks if the object is or is part of the diagram assistant figure. This
	 * is used to determine if the mouse is hovering over the diagram assistant.
	 * 
	 * @param object
	 *            the object in question
	 * @return True if the object in question is or is part of the diagram
	 *         assistant figure; false otherwise.
	 */
	protected abstract boolean isDiagramAssistant(Object object);

	/**
	 * Returns true if the diagram assistant is currently showing; false
	 * otherwise. This is used to determine if the diagram assistant should be
	 * shown or hidden at a given point in time.
	 * 
	 * @return true if the diagram assistant is showing; false otherwise
	 */
	protected abstract boolean isDiagramAssistantShowing();

	/**
	 * Shows the diagram assistant figure(s).
	 * 
	 * @param referencePoint
	 *            The reference point which may be used to determine where the
	 *            diagram assistant should be located. This is most likely the
	 *            current mouse location. This could be null, for example, when
	 *            the host gains focus via the space bar, in which case the
	 *            diagram assistant should be shown in a default location.
	 */
	protected abstract void showDiagramAssistant(Point referencePoint);

	/**
	 * Hides the diagram assistant figure(s).
	 */
	protected abstract void hideDiagramAssistant();

	/**
	 * Returns true if the diagram assistant should be shown; false otherwise.
	 * This can be overridden to check any other conditions which must be met
	 * prior to showing the diagram assistant.
	 * 
	 * @return true if the diagram assistant should be shown; false otherwise.
	 */
	protected boolean shouldShowDiagramAssistant() {
		getHost().isActive();
		isPreferenceOn();
		isHostEditable();
		// isHostResolvable();
		isDiagramPartActive();

		// return getHost().isActive() && isPreferenceOn() && isHostEditable()
		// && isHostResolvable() && isDiagramPartActive();
		// return true;
		
		return true;
	}

	/**
	 * Returns true if the preference to show this diagram assistant is on or if
	 * there is no applicable preference; false otherwise.
	 */
	protected boolean isPreferenceOn() {
		/* String prefName = getPreferenceName();
		if (prefName == null) {
			return true;
		}
		IPreferenceStore preferenceStore = (IPreferenceStore) ((IGraphicalEditPart) getHost())
				.getDiagramPreferencesHint().getPreferenceStore();
		return preferenceStore.getBoolean(prefName); */
		
		return true;
	}

	/**
	 * The preference name indicating if the preference should be on or off.
	 * This preference must be a boolean preference stored in the diagram
	 * preferences.
	 * 
	 * @return the preference name if applicable; null otherwise
	 */
	String getPreferenceName() {
		return null;
	}

	/**
	 * Checks if the host editpart is editable.
	 * 
	 * @return True if the host is editable; false otherwise.
	 */
	private boolean isHostEditable() {
		/*if (getHost() instanceof GraphicalEditPart) {
			return ((GraphicalEditPart) getHost()).isEditModeEnabled();
		} */
		return true;
	}

	/**
	 * Is the host's semantic reference resolvable (if applicable)?
	 * 
	 * @return true if the semantic reference is resolvable, true if there is no
	 *         semantic reference, and false otherwise
	 */
	private boolean isHostResolvable() {
		/* final View view = (View) getHost().getModel();
		if (view.getElement() != null) {
			Boolean retval;
			try {
				retval = (Boolean) ((IGraphicalEditPart) getHost())
						.getEditingDomain().runExclusive(
								new RunnableWithResult.Impl() {

									public void run() {
										setResult(ViewUtil
												.resolveSemanticElement(view) != null ? Boolean.TRUE
												: Boolean.FALSE);
									}
								});
				return retval.booleanValue();
			} catch (InterruptedException e) {
				Trace.catching(DiagramUIPlugin.getInstance(),
						DiagramUIDebugOptions.EXCEPTIONS_CATCHING, getClass(),
						"isHostResolvable", e); //$NON-NLS-1$
				Log.error(DiagramUIPlugin.getInstance(),
						DiagramUIStatusCodes.IGNORED_EXCEPTION_WARNING,
						"isHostResolvable", e); //$NON-NLS-1$
				return false;
			}
		} */
		return true;
	}

	/**
	 * Checks if the diagram part is active.
	 * 
	 * @return True if the diagram part is active; false otherwise.
	 */
	private boolean isDiagramPartActive() {
		/* IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();

		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				IWorkbenchPart activePart = page.getActivePart();
				if (activePart instanceof IDiagramWorkbenchPart) {
					return ((IDiagramWorkbenchPart) activePart)
							.getDiagramEditPart().getRoot().equals(
									((IGraphicalEditPart) getHost()).getRoot());
				}
			}
		}
		return false; */
		
		return true;
	}

	/**
	 * Shows the diagram assistant after a certain amount of time has passed.
	 * 
	 * @param delay
	 *            the delay in milliseconds
	 */
	protected void showDiagramAssistantAfterDelay(int delay) {
		if (shouldShowDiagramAssistant()) {
			Display.getCurrent().timerExec(delay,
					new ShowDiagramAssistantRunnable(getMouseLocation()));
		}
	}

	/**
	 * Hides the diagram assistant after a certain amount of time has passed.
	 * 
	 * @param delay
	 *            the delay in milliseconds
	 */
	protected void hideDiagramAssistantAfterDelay(int delay) {
		if (isDiagramAssistantShowing()) {
			Display.getCurrent().timerExec(delay, hideDiagramAssistantRunnable);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#activate()
	 */
	public void activate() {
		super.activate();
		((GraphicalEditPart)getHost()).getFigure().addMouseMotionListener(this);
		((GraphicalEditPart)getHost()).addEditPartListener(focusListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editpolicies.AbstractEditPolicy#deactivate()
	 */
	public void deactivate() {
		((GraphicalEditPart)getHost()).getFigure().removeMouseMotionListener(this);
		((GraphicalEditPart)getHost()).removeEditPartListener(focusListener);
		hideDiagramAssistant();
		super.deactivate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.draw2d.MouseMotionListener#mouseEntered(org.eclipse.draw2d.MouseEvent)
	 */
	public void mouseEntered(MouseEvent me) {
		setMouseLocation(me.getLocation());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.draw2d.MouseMotionListener#mouseExited(org.eclipse.draw2d.MouseEvent)
	 */
	public void mouseExited(MouseEvent me) {
		setMouseLocation(null);
		hideDiagramAssistantAfterDelay(getDisappearanceDelayUponExit());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.draw2d.MouseMotionListener#mouseMoved(org.eclipse.draw2d.MouseEvent)
	 */
	public void mouseMoved(MouseEvent me) {
		setMouseLocation(me.getLocation());

		// do not hide the diagram assistant if the user is hovering over it
		setAvoidHidingDiagramAssistant(isDiagramAssistant(me.getSource()));

		showDiagramAssistantAfterDelay(getAppearanceDelay());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.draw2d.MouseMotionListener#mouseHover(org.eclipse.draw2d.MouseEvent)
	 */
	public void mouseHover(MouseEvent me) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.draw2d.MouseMotionListener#mouseDragged(org.eclipse.draw2d.MouseEvent)
	 */
	public void mouseDragged(MouseEvent me) {
		// do nothing
	}

	/**
	 * Gets the amount of time to wait before showing the diagram assistant.
	 * 
	 * @return the time to wait in milliseconds
	 */
	protected int getAppearanceDelay() {
		return APPEARANCE_DELAY;
	}

	/**
	 * Gets the amount of time to wait before hiding the diagram assistant after
	 * it has been made visible.
	 * 
	 * @return the time to wait in milliseconds
	 */
	protected int getDisappearanceDelay() {
		return DISAPPEARANCE_DELAY;
	}

	/**
	 * Gets the amount of time to wait before hiding the diagram assistant after
	 * the user has moved the mouse outside of the editpart.
	 * 
	 * @return the time to wait in milliseconds
	 */
	protected int getDisappearanceDelayUponExit() {
		return DISAPPEARANCE_DELAY_UPON_EXIT;
	}

	/**
	 * Gets the current mouse location. This will be null if the mouse is
	 * outside the host and diagram assistant figure.
	 * 
	 * @return Returns the current mouse location
	 */
	protected Point getMouseLocation() {
		return mouseLocation;
	}

	/**
	 * Sets the current mouse location. If set to null, this implies that the
	 * mouse is outside the host and diagram assistant figure.
	 * 
	 * @param mouseLocation
	 *            the current mouse location
	 */
	protected void setMouseLocation(Point mouseLocation) {
		this.mouseLocation = mouseLocation;
	}

	/**
	 * Sets the flag to indicate that the diagram assistant should not be
	 * hidden.
	 * 
	 * @param avoidHidingDiagramAssistant
	 *            Flag to indicate that the diagram assistant should not be
	 *            hidden
	 */
	protected void setAvoidHidingDiagramAssistant(
			boolean avoidHidingDiagramAssistant) {
		this.avoidHidingDiagramAssistant = avoidHidingDiagramAssistant;
	}

	/**
	 * Returns true if the diagram assistant should not be hidden; false
	 * otherwise.
	 * 
	 * @return true if the diagram assistant should not be hidden; false
	 *         otherwise.
	 */
	protected boolean shouldAvoidHidingDiagramAssistant() {
		return avoidHidingDiagramAssistant;
	}

}
