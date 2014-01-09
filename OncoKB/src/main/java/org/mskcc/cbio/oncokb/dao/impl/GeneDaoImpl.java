/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.oncokb.dao.impl;

import java.util.List;
import org.mskcc.cbio.oncokb.dao.GeneDao;
import org.mskcc.cbio.oncokb.model.Gene;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * handling db requests for gene, gene_alias, and gene_label
 * @author jgao
 */
public class GeneDaoImpl extends HibernateDaoSupport implements GeneDao {
    
    /**
     * Get a gene by hugo symbol
     * @param symbol
     * @return gene object or null
     */
    public Gene getGeneByHugoSymbol(String symbol) {
        List list = getHibernateTemplate().find("from GeneImpl where hugoSymbol=?", symbol);
        
        if (list==null || list.isEmpty()) {
            return null;
        }
        
        return Gene.class.cast(list.get(0));
    }
    
    /**
     * Get a gene by Entrez Gene Id.
     * @param entrezGeneId
     * @return gene object or null.
     */
    public Gene getGeneByEntrezGeneId(int entrezGeneId) {
        List list = getHibernateTemplate().find("from GeneImpl where entrezGeneId=?",entrezGeneId);
        
        if (list==null || list.isEmpty()) {
            return null;
        }
        
        return Gene.class.cast(list.get(0));
    }
    
    /**
     * Save a gene to db.
     * @param gene 
     */
    public void saveOrUpdate(Gene gene) {
        getHibernateTemplate().saveOrUpdate(gene);
    }
}