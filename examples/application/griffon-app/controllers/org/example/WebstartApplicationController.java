package org.example;

import griffon.core.GriffonApplication;
import griffon.core.artifact.GriffonController;
import griffon.metadata.ArtifactProviderFor;
import org.codehaus.griffon.runtime.core.artifact.AbstractGriffonController;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import griffon.transform.Threading;

@ArtifactProviderFor(GriffonController.class)
public class WebstartApplicationController extends AbstractGriffonController {
    private WebstartApplicationModel model;

    @Inject
    public WebstartApplicationController(@Nonnull GriffonApplication application) {
        super(application);
    }

    public void setModel(WebstartApplicationModel model) {
        this.model = model;
    }

    @Threading(Threading.Policy.INSIDE_UITHREAD_ASYNC)
    public void click() {
        model.setClickCount(model.getClickCount() + 1);
    }
}