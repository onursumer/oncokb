package org.mskcc.cbio.oncokb.util;

import org.junit.Test;
import org.mskcc.cbio.oncokb.model.*;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Hongxin on 12/23/16.
 */
public class IndicatorUtilsTest {
    @Test
    public void testProcessQuery() throws Exception {
        // We dont check gene/variant/tumor type summaries here. The test will be done in SummaryUtilsTest.

        // Gene not exists
        Query query = new Query("FGF6", null, "V123M", null, "Pancreatic Adenocarcinoma", null, null, null);
        IndicatorQueryResp indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, true);
        assertTrue("The geneExist in the response should be set to false", indicatorQueryResp.getGeneExist() == false);
        assertEquals("The oncogenicity of should be empty", "", indicatorQueryResp.getOncogenic());
        assertTrue("No treatment should be given", indicatorQueryResp.getTreatments().size() == 0);

        // Oncogenic should always match with oncogenic summary, similar to likely oncogenic
        query = new Query("TP53", null, "R248Q", null, "Pancreatic Adenocarcinoma", null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, true);
        assertEquals("The oncogenicity is not matched in variant summary.", "The TP53 R248Q mutation is likely oncogenic.", indicatorQueryResp.getVariantSummary());
        query = new Query("KRAS", null, "V14I", null, "Pancreatic Adenocarcinoma", null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, true);
        assertEquals("The oncogenicity is not matched in variant summary.", "The KRAS V14I mutation is known to be oncogenic.", indicatorQueryResp.getVariantSummary());

        // Check fusion.
        query = new Query("BRAF", null, "CUL1-BRAF Fusion", null, "Ovarian Cancer", null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, true);
        assertEquals("The highest sensitive level of CUL1-BRAF fusion should be Level 3A", LevelOfEvidence.LEVEL_3A, indicatorQueryResp.getHighestSensitiveLevel());
        assertEquals("The oncogenicity of CUL1-BRAF fusion should be Likely Oncogenic", "Likely Oncogenic", indicatorQueryResp.getOncogenic());

        // Check unknown denominator fusion, it should return same data as querying specific fusion.
        Query query1 = new Query("CUL1-BRAF", null, null, "fusion", "Ovarian Cancer", null, null, null);
        IndicatorQueryResp indicatorQueryResp1 = IndicatorUtils.processQuery(query1, null, null, null, true);
        assertTrue("Oncogenic should be the same", indicatorQueryResp.getOncogenic().equals(indicatorQueryResp1.getOncogenic()));
        assertTrue("Treatments should be the same", indicatorQueryResp.getTreatments().equals(indicatorQueryResp1.getTreatments()));
        assertTrue("Highest sensitive level should be the same", LevelUtils.areSameLevels(indicatorQueryResp.getHighestSensitiveLevel(), indicatorQueryResp1.getHighestSensitiveLevel()));
        assertTrue("Highest resistance level should be the same", LevelUtils.areSameLevels(indicatorQueryResp.getHighestResistanceLevel(), indicatorQueryResp1.getHighestResistanceLevel()));

        // Check other significant level
        query = new Query("BRAF", null, "V600E", null, "Colorectal Cancer", null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, true);
        assertEquals("The highest sensitive level should be 2B", LevelOfEvidence.LEVEL_2B, indicatorQueryResp.getHighestSensitiveLevel());
        assertTrue("The highest resistance level should be null", indicatorQueryResp.getHighestResistanceLevel() == null);
        assertTrue("Should have one significant level", indicatorQueryResp.getOtherSignificantSensitiveLevels().size() == 1);
        assertEquals("The other significant level should be 3A", LevelOfEvidence.LEVEL_3A, indicatorQueryResp.getOtherSignificantSensitiveLevels().get(0));
        assertTrue(treatmentsContainLevel(indicatorQueryResp.getTreatments(), LevelOfEvidence.LEVEL_3A));
        assertTrue(treatmentsContainLevel(indicatorQueryResp.getTreatments(), LevelOfEvidence.LEVEL_2B));

        query = new Query("BRAF", null, "V600E", null, "Breast Cancer", null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, true);
        assertEquals("The highest sensitive level should be 2B", LevelOfEvidence.LEVEL_2B, indicatorQueryResp.getHighestSensitiveLevel());
        assertTrue("The highest resistance level should be null", indicatorQueryResp.getHighestResistanceLevel() == null);
        assertTrue("Shouldn't have any significant level", indicatorQueryResp.getOtherSignificantSensitiveLevels().size() == 0);
        assertTrue(treatmentsContainLevel(indicatorQueryResp.getTreatments(), LevelOfEvidence.LEVEL_2B));

        // Test for predicted oncogenic
        query = new Query("KRAS", null, "\tQ61Kfs*7", null, "Pancreatic Adenocarcinoma", null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, false);
        assertEquals("The oncogenicity should be 'Predicted Oncogenic'", Oncogenicity.PREDICTED.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The highest sensitive level should be 4, the level 3A evidence under Colorectal Cancer has been maked as NO propagation.",
            LevelOfEvidence.LEVEL_4, indicatorQueryResp.getHighestSensitiveLevel());

        query = new Query("KRAS", null, "\tQ61Kfs*7", null, "Colorectal Cancer", null, null, null);
        indicatorQueryResp = IndicatorUtils.processQuery(query, null, null, null, false);
        assertEquals("The oncogenicity should be 'Predicted Oncogenic'", Oncogenicity.PREDICTED.getOncogenic(), indicatorQueryResp.getOncogenic());
        assertEquals("The highest sensitive level should be 3A",
            LevelOfEvidence.LEVEL_3A, indicatorQueryResp.getHighestSensitiveLevel());
        assertEquals("The highest resistance level should be R1",
            LevelOfEvidence.LEVEL_R1, indicatorQueryResp.getHighestResistanceLevel());

    }

    private Boolean treatmentsContainLevel(List<IndicatorQueryTreatment> treatments, LevelOfEvidence level) {
        if (level == null || treatments == null) {
            return false;
        }

        for (IndicatorQueryTreatment treatment : treatments) {
            if (treatment.getLevel() != null && treatment.getLevel().equals(level)) {
                return true;
            }
        }
        return false;
    }
}
