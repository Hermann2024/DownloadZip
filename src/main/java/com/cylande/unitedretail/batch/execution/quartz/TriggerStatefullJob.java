package com.cylande.unitedretail.batch.execution.quartz;

import org.quartz.StatefulJob;

/**
 *  Job generique pour les jobs d�clench�s par trigger Cron (mode Statefull)
 */
public class TriggerStatefullJob extends TriggerJob implements StatefulJob
{
}
