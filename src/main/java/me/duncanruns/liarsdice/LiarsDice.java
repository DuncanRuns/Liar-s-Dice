package me.duncanruns.liarsdice;

import me.duncanruns.liarsdice.logic.LiarsDiceGame;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LiarsDice implements ModInitializer {

    public static final String MOD_ID = "liarsdice";
    public static final String MOD_NAME = "Liar's Dice";
    public static Logger LOGGER = LogManager.getLogger();
    private static volatile LiarsDiceGame liarsDiceGame;

    public static boolean hasDiceGame() {
        return getLiarsDiceGame() != null;
    }

    public static LiarsDiceGame getLiarsDiceGame() {
        return liarsDiceGame;
    }

    public static void setLiarsDiceGame(LiarsDiceGame liarsDiceGame) {
        if (getLiarsDiceGame() != null && !getLiarsDiceGame().hasEnded()) {
            getLiarsDiceGame().end();
        }
        LiarsDice.liarsDiceGame = liarsDiceGame;
    }

    public static void log(Level level, String message) {
        LOGGER.log(level, "[" + MOD_NAME + "] " + message);
    }

    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing");
        liarsDiceGame = null;
    }

}