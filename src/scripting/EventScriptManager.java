package scripting;

import handling.channel.ChannelServer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import tools.FileoutputUtil;

public class EventScriptManager extends AbstractScriptManager {
    private final Map<String, EventEntry> events = new LinkedHashMap<String, EventEntry>();
    private static final AtomicInteger runningInstanceMapId = new AtomicInteger(0);
    
    private static class EventEntry {
        public String script;
        public Invocable iv;
        public EventManager em;
        
        public EventEntry(final String script, final Invocable iv, final EventManager em) {
            this.script = script;
            this.iv = iv;
            this.em = em;
        }
    }
    
    public static final int getNewInstanceMapId() {
        return runningInstanceMapId.addAndGet(1);
    }

    public EventScriptManager(final ChannelServer cserv, final String[] scripts) {
        super();
        for (final String script : scripts) {
            if (!script.equals("")) {
                final Invocable iv = getInvocable("event/" + script + ".js", null);

                if (iv != null) {
                    events.put(script, new EventEntry(script, iv, new EventManager(cserv, iv, script)));
                }
            }
        }
    }

    public final EventManager getEventManager(final String event) {
        final EventEntry entry = events.get(event);
        if (entry == null) {
            return null;
        }
        return entry.em;
    }

    public final void init() {
        for (final EventEntry entry : events.values()) {
            try {
                ((ScriptEngine) entry.iv).put("em", entry.em);
                entry.iv.invokeFunction("init", (Object) null);
            } catch (final Exception ex) {
                System.out.println("Error initiating event: " + entry.script + ":" + ex);
                FileoutputUtil.log(FileoutputUtil.ScriptEx_Log, "Error initiating event: " + entry.script + ":" + ex);
            }
        }
    }

    public final void cancel() {
        for (final EventEntry entry : events.values()) {
            entry.em.cancel();
        }
    }
}
