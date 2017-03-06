package inventory.app;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

public class InventoryProps {

    private static Logger LOG = LogManager.getLogger(InventoryMain.class);

    /**
     * InventoryProps singleton
     *
     * @var InventoryProps
     */
    private static InventoryProps instance = null;

    /**
     * Configuration properties
     *
     * @var Properties
     */
    private Properties inventoryProps;

    /**
     * Returns or creates the InventoryProps singleton instance
     *
     * @return InventoryProps
     */
    public static InventoryProps getInstance() {
        if ( instance == null ) {
            return new InventoryProps();
        }

        return instance;
    }

    private InventoryProps() {
        this.inventoryProps = new Properties();
        try {
            this.inventoryProps.load(this.getClass().getResourceAsStream("/inventory.properties"));
        } catch (IOException e) {
            // This is pretty much the ultimate failure.. What do?
            LOG.fatal("Could not load inventory.properties from jarfile!");
            LOG.catching(e);
            System.exit(1);
        }

        LOG.debug("Loaded settings from `inventory.properties`:");

        for (String key : this.inventoryProps.stringPropertyNames()) {
            String value = this.inventoryProps.getProperty(key);
            LOG.debug(String.format(" %s ~> %s", key, value));
        }

        instance = this;
    }

    /**
     * Returns the Properties instance for this application
     *
     * @return Properties
     */
    public Properties getProps() {
        return this.inventoryProps;
    }
}
