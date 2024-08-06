package com.cylande.unitedretail.batch.execution.quartz;

import org.quartz.StatefulJob;

/**
 *  Job generique pour les jobs déclenchés par trigger Cron (mode Statefull)
 */
public class TriggerStatefullJob extends TriggerJob implements StatefulJob
{
}
