/*
 * tests.h
 *
 *  Created on: Jul 25, 2017
 *      Author: spinalvm
 */

#ifndef TESTS_H_
#define TESTS_H_

#define CHECK_PERIOD_MS 2000
#define CHECK_COUNT 3

#include "xxx.h"

void createTests(){
	xxx(tskIDLE_PRIORITY);
}

unsigned long checkTests(){
	unsigned long ulErrorFound = pdFALSE;

	/* Check all the demo and test tasks to ensure that they are all still
	running, and that none have detected an error. */

	if( xxx() != pdPASS )
	{
		bsp_putString("Error in xxx tasks \r\n");
		ulErrorFound |= ( 0x01UL << 1UL );
	}

	return ulErrorFound;
}

#endif /* TESTS_H_ */
