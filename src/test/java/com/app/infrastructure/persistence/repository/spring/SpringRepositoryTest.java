package com.app.infrastructure.persistence.repository.spring;

import com.app.infrastructure.persistence.exceptions.EntityNotFoundException;
import com.app.infrastructure.persistence.exceptions.IllegalUpdateException;
import com.app.infrastructure.persistence.criteria.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class Entity {
    private Long id;
    private String name;

    Entity() {

    }

    Entity(Long id) {
        this.id = id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

class Entity2 {
    private Long id;
    private String name;
}

public class SpringRepositoryTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private SpringRepository<Entity> repository;

    @InjectMocks
    private SpringRepository<Entity2> repository2;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        repository.setEntity(Entity.class);
        repository2.setEntity(Entity2.class);
    }

    @Test
    public void shouldGetEntityById_whenExists() {
        Long id = 1L;
        Entity mockedEntity = new Entity();
        when(entityManager.find(Entity.class, id)).thenReturn(mockedEntity);

        Entity result = repository.getById(id);

        assertEquals(mockedEntity, result);
    }

    @Test
    public void testGetById_NonExistingId_ThrowsEntityNotFoundException() {
        Long id = 100L;
        when(entityManager.find(Entity.class, id)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> repository.getById(id));
    }

    @Test
    public void shouldGetAllEntities_whenExist() {
        @SuppressWarnings("unchecked")
        TypedQuery<Entity> query = (TypedQuery<Entity>) mock(TypedQuery.class);

        Entity entity1 = new Entity();
        Entity entity2 = new Entity();
        List<Entity> entities = Arrays.asList(entity1, entity2);

        when(entityManager.createQuery("SELECT e FROM Entity e", Entity.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(entities);

        List<Entity> resultList = repository.getAll();

        assertEquals(entities.size(), resultList.size());
        assertEquals(entities, resultList);
    }

    @Test
    public void shouldCreateEntity() {
        Entity newEntity = new Entity();

        Entity createdEntity = repository.create(newEntity);

        verify(entityManager).persist(newEntity);

        assertEquals(newEntity, createdEntity);
    }

    @Test
    public void shouldUpdateEntity_whenExists() {
        Entity existingEntity = new Entity(1L);
        when(entityManager.find(Entity.class, 1L)).thenReturn(existingEntity);

        Entity updatedEntity = new Entity(1L);
        updatedEntity.setName("Updated Name");

        Entity resultEntity = repository.update(1L, updatedEntity);

        verify(entityManager).merge(existingEntity);

        assertEquals(updatedEntity.getName(), resultEntity.getName());
    }

    @Test
    public void shouldNotUpdateEntity_whenDoesNotExist() {
        when(entityManager.find(Entity.class, 1)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> {
            repository.update(1L, new Entity());
        });
    }

    @Test
    public void shouldDeleteEntity_whenExists() {
        Entity entity = new Entity();

        when(entityManager.find(Entity.class, 1L)).thenReturn(entity);

        assertTrue(repository.delete(1L));

        verify(entityManager).remove(any(Entity.class));
    }



    @Test
    public void shouldNotDeleteEntity_whenItDoesNotExist() {
        Entity entity = new Entity();

        when(entityManager.find(Entity.class, 1L)).thenReturn(null);

        assertThrows(EntityNotFoundException.class, () -> {
            assertTrue(repository.delete(1L));
        });
    }

    @Test
    public void shouldTranslateCriteria_whenUsingEquals() {
        @SuppressWarnings("unchecked")
        TypedQuery<Entity> query = mock(TypedQuery.class);

        when(entityManager.createQuery("SELECT entity FROM Entity entity WHERE name = :name", Entity.class)).thenReturn(query);
        when(query.setParameter("name", "foo")).thenReturn(query);

        Criteria criteria = new Criteria();
        criteria.equals("name", "foo");

        repository.getByFilter(criteria);

        verify(query).getResultList();
    }

    @Test
    public void shouldTranslateCriteria_whenUsingMultipleEquals() {
        @SuppressWarnings("unchecked")
        TypedQuery<Entity> query = mock(TypedQuery.class);

        when(entityManager.createQuery("SELECT entity FROM Entity entity WHERE name = :name AND id = :id", Entity.class)).thenReturn(query);
        when(query.setParameter("name", "foo")).thenReturn(query);
        when(query.setParameter("id", 1L)).thenReturn(query);

        Criteria criteria = new Criteria();
        criteria.equals("name", "foo");
        criteria.equals("id", 1L);

        repository.getByFilter(criteria);

        verify(query).getResultList();
    }

    @Test
    public void shouldTranslateCriteria_whenUsingLike() {
        @SuppressWarnings("unchecked")
        TypedQuery<Entity> query = mock(TypedQuery.class);

        when(entityManager.createQuery("SELECT entity FROM Entity entity WHERE name LIKE :name", Entity.class)).thenReturn(query);
        when(query.setParameter("name", "%foo%")).thenReturn(query);

        Criteria criteria = new Criteria();
        criteria.like("name", "foo");

        repository.getByFilter(criteria);

        verify(query).getResultList();
    }

    @Test
    public void shouldTranslateCriteria_whenUsingMultipleLike() {
        @SuppressWarnings("unchecked")
        TypedQuery<Entity> query = mock(TypedQuery.class);

        when(entityManager.createQuery("SELECT entity FROM Entity entity WHERE name LIKE :name AND id LIKE :id", Entity.class)).thenReturn(query);
        when(query.setParameter("name", "%foo%")).thenReturn(query);
        when(query.setParameter("id", "%"+1L+"%")).thenReturn(query);

        Criteria criteria = new Criteria();
        criteria.like("name", "foo");
        criteria.like("id", 1L);

        repository.getByFilter(criteria);

        verify(query).getResultList();
    }

    @Test
    public void shouldTranslateCriteria_whenUsingOrder() {
        @SuppressWarnings("unchecked")
        TypedQuery<Entity> query = mock(TypedQuery.class);

        when(entityManager.createQuery("SELECT entity FROM Entity entity ORDER BY name ASC", Entity.class)).thenReturn(query);

        Criteria criteria = new Criteria();
        criteria.order("name", OrderDirections.ASC);

        repository.getByFilter(criteria);

        verify(query).getResultList();
    }

    @Test
    public void shouldTranslateCriteria_whenUsingNotEquals() {
        @SuppressWarnings("unchecked")
        TypedQuery<Entity> query = mock(TypedQuery.class);

        when(entityManager.createQuery("SELECT entity FROM Entity entity WHERE name <> :name", Entity.class)).thenReturn(query);
        when(query.setParameter("name", "foo")).thenReturn(query);

        Criteria criteria = new Criteria();
        criteria.notEquals("name", "foo");

        repository.getByFilter(criteria);

        verify(query).getResultList();
    }

    @Test
    public void shouldTranslateCriteria_whenUsingMultipleNotEquals() {
        @SuppressWarnings("unchecked")
        TypedQuery<Entity> query = mock(TypedQuery.class);

        when(entityManager.createQuery("SELECT entity FROM Entity entity WHERE name <> :name AND id <> :id", Entity.class)).thenReturn(query);
        when(query.setParameter("name", "foo")).thenReturn(query);
        when(query.setParameter("id", 1L)).thenReturn(query);

        Criteria criteria = new Criteria();
        criteria.notEquals("name", "foo");
        criteria.notEquals("id", 1L);

        repository.getByFilter(criteria);

        verify(query).getResultList();
    }
}
