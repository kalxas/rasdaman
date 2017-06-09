package org.rasdaman.cis;

import java.math.BigDecimal;
import org.rasdaman.Application;
import org.rasdaman.domain.cis.RegularAxis;
import org.rasdaman.repository.interfaces.RegularAxisRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.Collection;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;


/**
 * Test persist RegularAxis with Hibernate
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {Application.class})
public class RegularAxisRepositoryTest {

    @Autowired
    private RegularAxisRepository regularAxisRepository;

    @Test
    public void checkResolution() {
        RegularAxis regularAxis = new RegularAxis();
        regularAxis.setResolution(new BigDecimal("2"));

        regularAxisRepository.save(regularAxis);

        Collection<RegularAxis> fetchedRegularAxis = regularAxisRepository.findOneByResolution(regularAxis.getResolution());

        Assert.isTrue(!fetchedRegularAxis.isEmpty());

        BigDecimal expected = fetchedRegularAxis.iterator().next().getResolution();
        BigDecimal actual = regularAxis.getResolution();
                
        Assert.isTrue(expected.equals(actual));
    }
}
