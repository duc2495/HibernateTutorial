package org.hibernate.tutorial;

import org.hibernate.Session;

import java.util.*;

import org.hibernate.tutorial.domain.Event;
import org.hibernate.tutorial.domain.Person;
import org.hibernate.tutorial.util.HibernateUtil;

public class EventManager {

	public static void main(String[] args) {
		EventManager mgr = new EventManager();
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		if (args[0].equals("store")) {
			mgr.createAndStoreEvent(session, "My Event", new Date());
			session.getTransaction().commit();
		} else if (args[0].equals("list")) {
			List events = mgr.listEvents(session);
			for (int i = 0; i < events.size(); i++) {
				Event theEvent = (Event) events.get(i);
				List persons = mgr.listPersonsWithEvent(session, theEvent.getId());
				System.out.println("Event: " + theEvent.getTitle() + " Time: " + theEvent.getDate());
				for (int j = 0; j < persons.size(); j++) {
					Person thePerson = (Person) persons.get(j);
					System.out.println(
							"\t-FisrtName: " + thePerson.getFirstname() + " LastName: " + thePerson.getLastname());
					System.out.println("\t\t-Email Address: ");
					for (String email : thePerson.getEmailAddresses()) {
						System.out.println("\t\t\t" + email);
					}

				}
			}
			session.getTransaction().commit();
		} else if (args[0].equals("addpersontoevent")) {
			Long eventId = mgr.createAndStoreEvent(session, "My Event", new Date());
			Long personId = mgr.createAndStorePerson(session, "Duc", "Nguyen");
			mgr.addPersonToEvent(session, personId, eventId);
			System.out.println("Added person " + personId + " to event " + eventId);
			session.getTransaction().commit();
		} else if (args[0].equals("addemail")) {
			mgr.addEmailToPerson(session, 1L, "duc2495@gmail.com");
			session.getTransaction().commit();
		}
		HibernateUtil.getSessionFactory().close();
	}

	private Long createAndStoreEvent(Session session, String title, Date theDate) {
		Event theEvent = new Event();
		theEvent.setTitle(title);
		theEvent.setDate(theDate);
		Long id = (Long) session.save(theEvent);
		return id;
	}

	private Long createAndStorePerson(Session session, String firstname, String lastname) {
		Person thePerson = new Person();
		thePerson.setFirstname(firstname);
		thePerson.setLastname(lastname);
		thePerson.setAge(20);
		Long id = (Long) session.save(thePerson);
		return id;
	}

	private List listEvents(Session session) {
		List result = session.createQuery("from Event").list();
		return result;
	}

	private List listPersonsWithEvent(Session session, Long idEvent) {
		List result = session.createQuery("from Person where id=" + idEvent).list();
		return result;
	}

	private void addPersonToEvent(Session session, Long personId, Long eventId) {
        Person aPerson = (Person) session.load(Person.class, personId);
        Event anEvent = (Event) session.load(Event.class, eventId);
        aPerson.addToEvent(anEvent);
	}

	private void addEmailToPerson(Session session, Long personId, String emailAddress) {
		Person aPerson = (Person) session.load(Person.class, personId);
		// adding to the emailAddress collection might trigger a lazy load of
		// the collection
		aPerson.getEmailAddresses().add(emailAddress);
	}

}