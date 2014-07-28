import griffon.util.AbstractMapResourceBundle;

import javax.annotation.Nonnull;
import java.util.Map;

import static java.util.Arrays.asList;
import static griffon.util.CollectionUtils.map;

public class Config extends AbstractMapResourceBundle {
    @Override
    protected void initialize(@Nonnull Map<String, Object> entries) {
        map(entries)
            .e("application", map()
                .e("title", "webstart-application")
                .e("startupGroups", asList("webstartApplication"))
                .e("autoShutdown", true)
            )
            .e("mvcGroups", map()
                .e("webstartApplication", map()
                    .e("model", "org.example.WebstartApplicationModel")
                    .e("view", "org.example.WebstartApplicationView")
                    .e("controller", "org.example.WebstartApplicationController")
                )
            );
    }
}