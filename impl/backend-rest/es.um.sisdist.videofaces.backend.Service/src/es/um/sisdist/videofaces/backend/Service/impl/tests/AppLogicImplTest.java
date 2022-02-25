/**
 * 
 */
package es.um.sisdist.videofaces.backend.Service.impl.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import es.um.sisdist.videofaces.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.videofaces.backend.dao.user.models.User;

/**
 * @author dsevilla
 *
 */
class AppLogicImplTest
{
	static AppLogicImpl impl;
	
	@BeforeAll 
	static void setup()
	{
		impl = AppLogicImpl.getInstance();
	}
	
	@Test
	void testDefaultUser()
	{
		Optional<User> u = impl.getUserByEmail("dsevilla@um.es");
		assertEquals(u.get().getEmail(), "dsevilla@um.es");
	}

}
