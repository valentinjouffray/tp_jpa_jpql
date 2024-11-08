package fr.diginamic;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ActeurRepositoryTest {

    private static EntityManagerFactory emf;
    private EntityManager em;

    @BeforeAll
    public static void initDatabase() {
        emf = Persistence.createEntityManagerFactory("movie_db");
        EntityManager em = emf.createEntityManager();

        try {

            if (em.createQuery("FROM Acteur").getResultList().isEmpty()) {
                em.getTransaction().begin();
                Path home = Paths.get(Objects.requireNonNull(ActeurRepositoryTest.class.getClassLoader().getResource("data.sql")).toURI());
                String[] queries = String.join("\n", Files.readAllLines(home)).split(";");
                for (String query : queries) {
                    em.createNativeQuery(query).executeUpdate();
                }
                em.getTransaction().commit();
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        em.close();
    }

    @AfterAll
    public static void fermetureRessources() {
        emf.close();
    }

    /**
     * Extraire tous les acteurs triés dans l'ordre alphabétique des identités
     */
    @Test
    public void testExtraireActeursTriesParIdentite() {

        TypedQuery<Acteur> query = em.createQuery("SELECT a FROM Acteur a order by a.identite", Acteur.class);
        List<Acteur> acteurs = query.getResultList();

        assertEquals(1137, acteurs.size());
        assertEquals("A.J. Danna", acteurs.get(0).getIdentite());
    }

    /**
     * Extraire l'actrice appelée Marion Cotillard
     */
    @Test
    public void testExtraireActeursParIdentite() {
        TypedQuery<Acteur> query = em.createQuery("SELECT a FROM Acteur a WHERE a.identite like :identite", Acteur.class);
        query.setParameter("identite", "Marion Cotillard");
        List<Acteur> acteurs = query.getResultList();

        assertEquals(1, acteurs.size());
        assertEquals("Marion Cotillard", acteurs.get(0).getIdentite());
    }

    /**
     * Extraire la liste des acteurs dont l'année de naissance est 1985. Astuce :
     * fonction year(...)
     */
    @Test
    public void testExtraireActeursParAnneeNaissance() {
        TypedQuery<Acteur> query = em.createQuery("SELECT a FROM Acteur a where year(a.anniversaire) = :annee", Acteur.class);
        query.setParameter("annee", 1985);
        List<Acteur> acteurs = query.getResultList();

        assertEquals(10, acteurs.size());
    }

    /**
     * Extraire la liste des actrices ayant joué le rôle d'Harley QUINN
     */
    @Test
    public void testExtraireActeursParRole() {

        TypedQuery<Acteur> query = em.createQuery("SELECT a FROM Acteur a JOIN a.roles r WHERE r.nom LIKE :role_name", Acteur.class);
        query.setParameter("role_name", "Harley Quinn");
        List<Acteur> acteurs = query.getResultList();

        assertEquals(1, acteurs.size());
        assertEquals("Margot Robbie", acteurs.get(0).getIdentite());
    }

    /**
     * Extraire la liste de tous les acteurs ayant joué dans un film paru en 2015.
     */
    @Test
    public void testExtraireActeursParFilmParuAnnee() {
        TypedQuery<Acteur> query = em.createQuery("SELECT a FROM Acteur a JOIN a.roles r JOIN r.film f WHERE f.annee = :annee", Acteur.class);
        query.setParameter("annee", 2015);
        List<Acteur> acteurs = query.getResultList();
        assertEquals(119, acteurs.size());
    }

    /**
     * Extraire la liste de tous les acteurs ayant joué dans un film français
     */
    @Test
    public void testExtraireActeursParPays() {
        TypedQuery<Acteur> query = em.createQuery("SELECT a FROM Acteur a JOIN a.roles r JOIN r.film f JOIN f.pays p WHERE p.nom = :pays", Acteur.class);
        query.setParameter("pays", "France");
        List<Acteur> acteurs = query.getResultList();
        assertEquals(158, acteurs.size());
    }

    /**
     * Extraire la liste de tous les acteurs ayant joué dans un film français paru
     * en 2017
     */
    @Test
    public void testExtraireActeursParListePaysEtAnnee() {
        TypedQuery<Acteur> query = em.createQuery("SELECT a FROM Acteur a JOIN a.roles r JOIN r.film f JOIN f.pays p WHERE p.nom = :pays AND f.annee = :annee", Acteur.class);
        query.setParameter("pays", "France");
        query.setParameter("annee", 2017);
        List<Acteur> acteurs = query.getResultList();
        assertEquals(24, acteurs.size());
    }

    /**
     * Extraire la liste de tous les acteurs ayant joué dans un film réalisé par
     * Ridley Scott entre les années 2010 et 2020
     */
    @Test
    public void testExtraireParRealisateurEntreAnnee() {
        TypedQuery<Acteur> query = em.createQuery("SELECT a FROM Acteur a JOIN a.roles r JOIN r.film f JOIN f.realisateurs re WHERE re.identite = :realisateur AND f.annee BETWEEN :annee1 AND :annee2", Acteur.class);
        query.setParameter("realisateur", "Ridley Scott");
        query.setParameter("annee1", 2010);
        query.setParameter("annee2", 2020);
        List<Acteur> acteurs = query.getResultList();
        assertEquals(27, acteurs.size());
    }

    /**
     * Extraire la liste de tous les réalisateurs ayant réalisé un film dans lequel Brad Pitt a joué
     */
    @Test
    public void testExtraireRealisateursParActeur() {
        TypedQuery<Realisateur> query = em.createQuery("SELECT r FROM Realisateur r JOIN r.films f JOIN f.roles ro JOIN ro.acteur a WHERE a.identite = :acteur", Realisateur.class);
        query.setParameter("acteur", "Brad Pitt");
        List<Realisateur> acteurs = query.getResultList();
        assertEquals(6, acteurs.size());
    }

    @BeforeEach
    public void ouvertureEm() {
        em = emf.createEntityManager();
    }

    @AfterEach
    public void fermetureEm() {
        em.close();
    }
}