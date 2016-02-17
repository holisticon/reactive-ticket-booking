package de.holisticon.showcase.ticketbooking.model

/**
 * <p>
 * A reusable representation of an address.
 * </p>
 *
 * <p>
 * Addresses are used in many places in an application, so to observe the DRY principle, we model Address as an embeddable
 * entity. An embeddable entity appears as a child in the object model, but no relationship is established in the RDBMS..
 * </p>
 *
 * @author Marius Bogoevici
 * @author Pete Muir
 */
final case class Address(
  street: Option[String],
  city: String,
  country: String
)
