package it.eng.intercenter.oxalis.integration.dto.factory;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import it.eng.intercenter.oxalis.integration.dto.OxalisQuartzCommand;
import it.eng.intercenter.oxalis.integration.dto.OxalisQuartzJobKey;
import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisQuartzCommandActionEnum;
import it.eng.intercenter.oxalis.integration.dto.enumerator.OxalisQuartzCommandScopeEnum;

/**
 * @author Manuel Gozzi
 * @date 4 set 2019
 * @time 15:48:36
 */
public class OxalisQuartzCommandFactoryTest {

	@Test
	public void testCommandFactory() {

		OxalisQuartzJobKey tc_job_key1 = new OxalisQuartzJobKey();
		tc_job_key1.setJobGroup("group1");
		tc_job_key1.setJobName("name1");

		OxalisQuartzJobKey tc_job_key2 = new OxalisQuartzJobKey();
		tc_job_key2.setJobGroup("group2");
		tc_job_key2.setJobName("name2");

		List<OxalisQuartzJobKey> jobs = new ArrayList<>();
		jobs.add(tc_job_key1);
		jobs.add(tc_job_key2);

		OxalisQuartzCommand tc_start1 = OxalisQuartzCommandFactory.prepare().asStart().overAllJobs().build();
		OxalisQuartzCommand tc_start2 = OxalisQuartzCommandFactory.prepare().asStart().overSingleJob(tc_job_key1).build();
		OxalisQuartzCommand tc_start3 = OxalisQuartzCommandFactory.prepare().asStart().overTheseJobs(jobs).build();

		Assert.assertEquals(OxalisQuartzCommandActionEnum.START, tc_start1.getAction());
		Assert.assertEquals(OxalisQuartzCommandActionEnum.START, tc_start2.getAction());
		Assert.assertEquals(OxalisQuartzCommandActionEnum.START, tc_start3.getAction());

		Assert.assertEquals(OxalisQuartzCommandScopeEnum.ALL_JOBS, tc_start1.getScope());
		Assert.assertEquals(OxalisQuartzCommandScopeEnum.SINGLE_JOB, tc_start2.getScope());
		Assert.assertEquals(OxalisQuartzCommandScopeEnum.JOB_LIST, tc_start3.getScope());

		Assert.assertNull(tc_start1.getJobKeys());
		Assert.assertNotNull(tc_start2.getJobKeys());
		Assert.assertEquals(1, tc_start2.getJobKeys().size());
		Assert.assertNotNull(tc_start3.getJobKeys());
		Assert.assertEquals(2, tc_start3.getJobKeys().size());

		OxalisQuartzCommand tc_stop1 = OxalisQuartzCommandFactory.prepare().asStop().overAllJobs().build();
		OxalisQuartzCommand tc_stop2 = OxalisQuartzCommandFactory.prepare().asStop().overSingleJob(tc_job_key1).build();
		OxalisQuartzCommand tc_stop3 = OxalisQuartzCommandFactory.prepare().asStop().overTheseJobs(jobs).build();

		Assert.assertEquals(OxalisQuartzCommandActionEnum.STOP, tc_stop1.getAction());
		Assert.assertEquals(OxalisQuartzCommandActionEnum.STOP, tc_stop2.getAction());
		Assert.assertEquals(OxalisQuartzCommandActionEnum.STOP, tc_stop3.getAction());

		Assert.assertEquals(OxalisQuartzCommandScopeEnum.ALL_JOBS, tc_stop1.getScope());
		Assert.assertEquals(OxalisQuartzCommandScopeEnum.SINGLE_JOB, tc_stop2.getScope());
		Assert.assertEquals(OxalisQuartzCommandScopeEnum.JOB_LIST, tc_stop3.getScope());

		Assert.assertNull(tc_stop1.getJobKeys());
		Assert.assertNotNull(tc_stop2.getJobKeys());
		Assert.assertEquals(1, tc_stop2.getJobKeys().size());
		Assert.assertNotNull(tc_stop3.getJobKeys());
		Assert.assertEquals(2, tc_stop3.getJobKeys().size());

		OxalisQuartzCommand tc_view1 = OxalisQuartzCommandFactory.prepare().asView().overAllJobs().build();
		OxalisQuartzCommand tc_view2 = OxalisQuartzCommandFactory.prepare().asView().overSingleJob(tc_job_key1).build();
		OxalisQuartzCommand tc_view3 = OxalisQuartzCommandFactory.prepare().asView().overTheseJobs(jobs).build();

		Assert.assertEquals(OxalisQuartzCommandActionEnum.VIEW, tc_view1.getAction());
		Assert.assertEquals(OxalisQuartzCommandActionEnum.VIEW, tc_view2.getAction());
		Assert.assertEquals(OxalisQuartzCommandActionEnum.VIEW, tc_view3.getAction());

		Assert.assertEquals(OxalisQuartzCommandScopeEnum.ALL_JOBS, tc_view1.getScope());
		Assert.assertEquals(OxalisQuartzCommandScopeEnum.SINGLE_JOB, tc_view2.getScope());
		Assert.assertEquals(OxalisQuartzCommandScopeEnum.JOB_LIST, tc_view3.getScope());

		Assert.assertNull(tc_view1.getJobKeys());
		Assert.assertNotNull(tc_view2.getJobKeys());
		Assert.assertEquals(1, tc_view2.getJobKeys().size());
		Assert.assertNotNull(tc_view3.getJobKeys());
		Assert.assertEquals(2, tc_view3.getJobKeys().size());

	}

}
