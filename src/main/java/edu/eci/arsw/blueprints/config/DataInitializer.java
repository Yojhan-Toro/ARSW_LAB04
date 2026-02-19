package edu.eci.arsw.blueprints.config;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistence;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final BlueprintPersistence persistence;

    public DataInitializer(BlueprintPersistence persistence) {
        this.persistence = persistence;
    }

    @Override
    public void run(ApplicationArguments args) {
        seed("john", "house",   List.of(new Point(0,0),  new Point(10,0), new Point(10,10), new Point(0,10)));
        seed("john", "garage",  List.of(new Point(5,5),  new Point(15,5), new Point(15,15)));
        seed("jane", "garden",  List.of(new Point(2,2),  new Point(3,4),  new Point(6,7)));
    }

    private void seed(String author, String name, List<Point> pts) {
        try {
            persistence.saveBlueprint(new Blueprint(author, name, pts));
            log.info("Seeded blueprint: {}/{}", author, name);
        } catch (BlueprintPersistenceException e) {
            log.debug("Blueprint {}/{} already exists — skipping seed.", author, name);
        }
    }
}
