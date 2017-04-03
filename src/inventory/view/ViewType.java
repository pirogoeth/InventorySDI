package inventory.view;

import inventory.controller.*;
import inventory.res.Res;
import inventory.util.Reflect;
import javafx.scene.Parent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public enum ViewType {
    ROOT(Res.root_view, Root.class),
    AUTHOR_LIST(Res.author_list, AuthorList.class),
    AUTHOR_DETAIL(Res.author_detailed, AuthorDetail.class),
    BOOK_LIST(Res.book_list, BookList.class),
    BOOK_DETAIL(Res.book_detailed, BookDetail.class),
    LIBRARY_LIST(Res.library_list, LibraryList.class),
    LIBRARY_DETAIL(Res.library_detailed, LibraryDetail.class),
    WAITING_PANE(Res.waiting_pane, WaitingPane.class),
    AUDIT_VIEW(Res.audit_log, AuditView.class);

    private static Logger LOG = LogManager.getLogger(ViewType.class);
    private URL resUrl;
    private Class controllerCls;
    private static Map<String, Parent> viewInst = new HashMap<>();
    private static Map<String, Object> ctrlInst = new HashMap<>();

    ViewType(URL resUrl, Class controllerCls) {
        this.resUrl = resUrl;
        this.controllerCls = controllerCls;
    }

    public URL getViewUrl() {
        return this.resUrl;
    }

    public Class getControllerClass() {
        return this.controllerCls;
    }

    public Object initController(Object...args) {
        // Use types to construct a type array
        Class[] argTypes = Reflect.buildParamTypeList(args);

        try {
            Constructor cons = this.controllerCls.getConstructor(argTypes);
            this.setController(cons.newInstance(args));
            return this.getController();
        } catch (Exception ex) {
            // double whelp
            LOG.catching(ex);
        }

        return null;
    }

    public Parent getViewInst() {
        return viewInst.get(this.name());
    }

    void setViewInst(Parent view) {
        viewInst.put(this.name(), view);
    }

    public Object getController() {
        return ctrlInst.get(this.name());
    }

    void setController(Object o) {
        ctrlInst.put(this.name(), o);
    }

    public boolean isContentModified() {
        Object ctrl = this.getController();
        if ( !( ctrl instanceof ContentModifiable ) ) {
            return false;
        }

        ContentModifiable cm = (ContentModifiable) ctrl;
        return cm.isContentModified();
    }
}
