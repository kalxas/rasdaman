package org.rasdaman.cis;

import java.math.BigDecimal;
import java.util.ArrayList;
import org.rasdaman.domain.cis.IrregularAxis;
import org.rasdaman.repository.interfaces.IrregularAxisRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.LinkedList;
import java.util.List;
import org.junit.runner.RunWith;
import org.rasdaman.ApplicationMain;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test persist IrregularAxis with Hibernate
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {ApplicationMain.class})
// Spring Boot 1.4 must use this new syntax and it will need to point to main class 
// which contains Repository in sub package.
public class IrregularAxisRepositoryTest {

    @Autowired
    private IrregularAxisRepository irregularAxisRepository;

    @Test
    public void checkDirectPositions() {
        IrregularAxis irregularAxis = createIrregularAxis();
        irregularAxisRepository.save(irregularAxis);

        Assert.notNull(irregularAxis.getId());

        List<String> directPositions = new LinkedList<>();
        directPositions.add(new String("2"));

        Assert.isTrue(directPositions.equals(irregularAxis.getDirectPositionsNumber()));
    }

    private IrregularAxis createIrregularAxis() {
        IrregularAxis irregularAxis = new IrregularAxis();
        List<BigDecimal> directPositions = new ArrayList<>();

        directPositions.add(new BigDecimal("2"));
        irregularAxis.setDirectPositions(directPositions);

        return irregularAxis;
    }

}
