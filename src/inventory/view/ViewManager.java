package inventory.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ViewManager {

    public interface ViewStateModifier {
        ViewState modify(ViewState vs);
    }

    public static class ViewState {
        private Parent navView;
        private Parent centerView;

        public ViewState(Parent nav, Parent center) {
            this.navView = nav;
            this.centerView = center;
        }

        public Parent getNav() {
            return this.navView;
        }

        public Parent getCenter() {
            return this.centerView;
        }
    }

    private static final Logger LOG = LogManager.getLogger(ViewManager.class);
    private static ViewManager instance = null;

    public static ViewManager getInstance() {
        if ( instance == null ) {
            return new ViewManager();
        }

        return instance;
    }

    private List<ViewState> viewStateHistory = new ArrayList<>();

    private ViewManager() {
        instance = this;
    }

    /**
     * Returns the top (active) ViewState.
     *
     * @return ViewState
     */
    public ViewState getCurrentViewState() {
        if ( this.viewStateHistory.isEmpty() ) {
            return null;
        }

        return this.viewStateHistory.get(0);
    }

    /**
     * Simple method to create a new view which can be displayed.
     *
     * @param vType ViewType
     * @param modelData Model object to be passed to the controller.
     * @return success?
     */
    public boolean initView(ViewType vType, Object modelData) {
        Object controller;

        if ( modelData != null ) {
            controller = vType.initController(modelData);
        } else {
            controller = vType.initController();
        }

        FXMLLoader loader = new FXMLLoader(vType.getViewUrl());
        loader.setController(controller);

        Parent view;
        try {
            view = loader.load();
        } catch (Exception ex) {
            LOG.catching(ex);
            return false;
        }

        vType.setViewInst(view);

        return true;
    }

    /**
     * Simple method to create a new view which can be displayed.
     *
     * @param vType ViewType
     * @return success?
     */
    public boolean initView(ViewType vType) {
        return this.initView(vType, null);
    }

    /**
     * Method which takes a ViewState, pushes it onto the stack,
     * and displays it.
     *
     * @param vs ViewState view layout
     * @return boolean
     */
    public boolean changeView(ViewState vs) {
        BorderPane rootPane = (BorderPane) ViewType.ROOT.getViewInst();
        if ( rootPane == null ) {
            if ( !this.initView(ViewType.ROOT, null) ) {
                // uhh
                LOG.warn("Could not initialize root view...");
                return false;
            }

            rootPane = (BorderPane) ViewType.ROOT.getViewInst();
        }

        rootPane.setLeft(vs.navView);
        rootPane.setCenter(vs.centerView);

        this.viewStateHistory.add(0, vs);

        return true;
    }

    /**
     * Accepts two new Parent views to create a new ViewState.
     * Pushes the new ViewState to the front of the history list.
     * Displays the new ViewState.
     *
     * @param navView Left-side navigation view
     * @param centerView Center focus view
     * @return boolean success
     */
    public boolean changeView(Parent navView, Parent centerView) {
        ViewState vs = new ViewState(navView, centerView);

        return this.changeView(vs);
    }

    /**
     * Pops off the currently active view (index 0) and
     * restores the displayed views to the new state at index 0.
     *
     * @return ViewState Previous view state
     */
    public ViewState restorePreviousView() {
        // Pop the current view.
        ViewState cur = this.viewStateHistory.remove(0);
        if ( cur == null ) {
            return null;
        }

        // Remove the previous because `changeView` is just going to
        // put it back on the stack.
        ViewState prev = this.viewStateHistory.remove(0);
        if ( prev == null ) {
            return null;
        }

        this.changeView(prev);

        return cur;
    }

    /**
     * Takes a ViewStateModifier which returns a new ViewState
     * with modifications. Applies the new ViewState to the
     * application view.
     *
     * @param vsm ViewStateMofifier
     * @return success?
     */
    public boolean modifyViewState(ViewStateModifier vsm) {
        try {
            // Clear the active views
            BorderPane rootPane = (BorderPane) ViewType.ROOT.getViewInst();
            if ( rootPane == null ) {
                // uhhh
                return false;
            }

            rootPane.setLeft(null);
            rootPane.setCenter(null);

            // Do the view state modification
            ViewState newVs = vsm.modify(this.getCurrentViewState());
            this.changeView(newVs);
            return true;
        } catch (Exception ex) {
            LOG.catching(ex);
            return false;
        }
    }

    /**
     * Clears all active view state.
     */
    public void clearAll() {
        // Clear the ViewState history
        this.viewStateHistory.clear();

        // Clear the active views
        BorderPane rootPane = (BorderPane) ViewType.ROOT.getViewInst();
        if ( rootPane == null ) {
            // uhhh
            return;
        }

        rootPane.setLeft(null);
        rootPane.setCenter(null);
    }

    public boolean viewIsActive(ViewType vType) {
        ViewState current = this.getCurrentViewState();

        Parent viewReq = vType.getViewInst();
        if ( viewReq == null ) {
            return false;
        }

        if ( current.getNav() == viewReq || current.getCenter() == viewReq ) {
            return true;
        } else {
            return false;
        }
    }
}
