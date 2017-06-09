package org.rasdaman.cis;

import org.rasdaman.Application;
import org.rasdaman.domain.cis.RasdamanRangeSet;
import org.rasdaman.repository.interfaces.RasdamanRangeSetRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.math.BigInteger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test persist RasdamanRangeSetRepository with Hibernate
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {Application.class})
public class RasdamanRangeSetRepositoryTest {

    @Autowired private
    RasdamanRangeSetRepository rasdamanRangeSetRepository;

    @Test
    public void checkOid() {
        RasdamanRangeSet rasdamanRangeSet = createRangeSet();
        rasdamanRangeSetRepository.save(rasdamanRangeSet);

        Assert.isTrue(rasdamanRangeSet.getOid() == rasdamanRangeSetRepository.findOneByOid(BigInteger.ONE).getOid());
    }

    RasdamanRangeSet createRangeSet() {
        RasdamanRangeSet rasdamanRangeSet = new RasdamanRangeSet();
        rasdamanRangeSet.setCollectionName("CollectionNameTest");
        rasdamanRangeSet.setCollectionType("CollectionTypeTest");
        rasdamanRangeSet.setOid(new Long("1"));
        rasdamanRangeSet.setMddType("MddTypeTest");

        return rasdamanRangeSet;
    }
}
