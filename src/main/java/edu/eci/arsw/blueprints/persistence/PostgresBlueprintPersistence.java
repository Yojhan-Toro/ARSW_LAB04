package edu.eci.arsw.blueprints.persistence;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.entity.BlueprintEntity;
import edu.eci.arsw.blueprints.persistence.entity.PointEmbeddable;
import edu.eci.arsw.blueprints.persistence.repository.BlueprintJpaRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Repository
@Primary
public class PostgresBlueprintPersistence implements BlueprintPersistence {

    private final BlueprintJpaRepository repo;

    public PostgresBlueprintPersistence(BlueprintJpaRepository repo) {
        this.repo = repo;
    }


    private static BlueprintEntity toEntity(Blueprint bp) {
        List<PointEmbeddable> pts = bp.getPoints().stream()
                .map(p -> new PointEmbeddable(p.x(), p.y()))
                .toList();
        return new BlueprintEntity(bp.getAuthor(), bp.getName(), pts);
    }

    private static Blueprint toDomain(BlueprintEntity e) {
        List<Point> pts = e.getPoints().stream()
                .map(p -> new Point(p.getX(), p.getY()))
                .toList();
        return new Blueprint(e.getAuthor(), e.getName(), pts);
    }

    @Override
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {
        if (repo.findByAuthorAndName(bp.getAuthor(), bp.getName()).isPresent()) {
            throw new BlueprintPersistenceException(
                    "Blueprint already exists: %s/%s".formatted(bp.getAuthor(), bp.getName()));
        }
        repo.save(toEntity(bp));
    }

    @Override
    public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
        return repo.findByAuthorAndName(author, name)
                .map(PostgresBlueprintPersistence::toDomain)
                .orElseThrow(() -> new BlueprintNotFoundException(
                        "Blueprint not found: %s/%s".formatted(author, name)));
    }

    @Override
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {
        List<BlueprintEntity> entities = repo.findByAuthor(author);
        if (entities.isEmpty()) {
            throw new BlueprintNotFoundException("No blueprints for author: " + author);
        }
        Set<Blueprint> result = new HashSet<>();
        for (BlueprintEntity e : entities) result.add(toDomain(e));
        return result;
    }

    @Override
    public Set<Blueprint> getAllBlueprints() {
        Set<Blueprint> result = new HashSet<>();
        for (BlueprintEntity e : repo.findAll()) result.add(toDomain(e));
        return result;
    }

    @Override
    public void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException {
        BlueprintEntity entity = repo.findByAuthorAndName(author, name)
                .orElseThrow(() -> new BlueprintNotFoundException(
                        "Blueprint not found: %s/%s".formatted(author, name)));
        entity.getPoints().add(new PointEmbeddable(x, y));
        repo.save(entity);
    }
}
