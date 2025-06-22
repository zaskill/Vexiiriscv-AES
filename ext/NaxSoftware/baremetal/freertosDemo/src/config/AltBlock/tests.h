/*
 * tests.h
 *
 *  Created on: Jul 25, 2017
 *      Author: spinalvm
 */

#ifndef TESTS_H_
#define TESTS_H_

#define CHECK_PERIOD_MS 3000
#define CHECK_COUNT 3

#include "AltBlock.h"

void createTests(){
	vCreateAltBlockTimeTasks();
}

unsigned long checkTests(){
	unsigned long ulErrorFound = pdFALSE;

	/* Check all the demo and test tasks to ensure that they are all still
	running, and that none have detected an error. */

	if( xAreAltBlockTimeTestTasksStillRunning() != pdPASS )
	{
		bsp_putString("Error in AltBlock tasks \r\n");
		ulErrorFound |= ( 0x01UL << 1UL );
	}

	return ulErrorFound;
}

#endif /* TESTS_H_ */
