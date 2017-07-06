package org.hibernate.tutorial;

import org.hibernate.Session;

import java.util.*;

import org.hibernate.tutorial.domain.Event;
import org.hibernate.tutorial.domain.Person;
import org.hibernate.tutorial.util.HibernateUtil;

public class EventManager {

	public static void main(String[] args) {
		EventManager mgr = new EventManager();

		if (args[0].equals("store")) {
			mgr.createAndStoreEvent("My Event", new Date());
		} else if (args[0].equals("list")) {
			List events = mgr.listEvents();
			for (int i = 0; i < events.size(); i++) {
				Event theEvent = (Event) events.get(i);
				List persons = mgr.listPersonsWithEvent(theEvent.getId());
				System.out.println("Event: " + theEvent.getTitle() + " Time: " + theEvent.getDate());
				for (int j = 0; j < persons.size(); j++) {
					Person thePerson = (Person) persons.get(j);
					System.out.println(
							"\t-FisrtName: " + thePerson.getFirstname() + " LastName: " + thePerson.getLastname());
//					System.out.println("\t\t-Email Address: ");
//					for (String email : thePerson.getEmailAddresses()) {
//						System.out.println("\t\t\t" + email);
//					}

				}
			}
		} else if (args[0].equals("addpersontoevent")) {
			Long eventId = mgr.createAndStoreEvent("My Event", new Date());
			Long personId = mgr.createAndStorePerson("Duc", "Nguyen");
			mgr.addPersonToEvent(personId, eventId);
			System.out.println("Added person " + personId + " to event " + eventId);
		} else if (args[0].equals("addemail")) {
			mgr.addEmailToPerson(1L, "duc2495@gmail.com");
		}

		HibernateUtil.getSessionFactory().close();
	}

	private Long createAndStoreEvent(String title, Date theDate) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		Event theEvent = new Event();
		theEvent.setTitle(title);
		theEvent.setDate(theDate);
		Long id = (Long) session.save(theEvent);

		session.getTransaction().commit();
		return id;
	}

	private Long createAndStorePerson(String firstname, String lastname) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		Person thePerson = new Person();
		thePerson.setFirstname(firstname);
		thePerson.setLastname(lastname);
		thePerson.setAge(20);
		Long id = (Long) session.save(thePerson);

		session.getTransaction().commit();
		return id;
	}

	private List listEvents() {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		List result = session.createQuery("from Event").list();
		session.getTransaction().commit();
		return result;
	}

	private List listPersonsWithEvent(Long idEvent) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		List result = session.createQuery("from Person where id=" + idEvent).list();
		session.getTransaction().commit();
		return result;
	}

	private void addPersonToEvent(Long personId, Long eventId) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		Person aPerson = (Person) session
				.createQuery("select p from Person p left join fetch p.events where p.id = :pid")
				.setParameter("pid", personId).uniqueResult(); // Eager fetch
																// the
																// collection so
																// we can use it
																// detached
		Event anEvent = (Event) session.load(Event.class, eventId);

		session.getTransaction().commit();

		// End of first unit of work

		aPerson.getEvents().add(anEvent); // aPerson (and its collection) is
											// detached

		// Begin second unit of work

		Session session2 = HibernateUtil.getSessionFactory().getCurrentSession();
		session2.beginTransaction();
		session2.update(aPerson); // Reattachment of aPerson

		session2.getTransaction().commit();
	}

	private void addEmailToPerson(Long personId, String emailAddress) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		Person aPerson = (Person) session.load(Person.class, personId);
		// adding to the emailAddress collection might trigger a lazy load of
		// the collection
		aPerson.getEmailAddresses().add(emailAddress);

		session.getTransaction().commit();
	}

}