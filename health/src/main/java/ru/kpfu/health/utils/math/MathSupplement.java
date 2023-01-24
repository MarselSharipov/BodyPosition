/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 *  Copyright (c) 2009 by Vinnie Falco
 *  Copyright (c) 2016 by Bernd Porr
 */

package ru.kpfu.health.utils.math;

import org.apache.commons.math3.complex.Complex;

/**
 * 
 * Useful math functions which come back over and over again
 *
 */
public class MathSupplement {
	public static Complex addmul(Complex c, double v, Complex c1) {
		return new Complex(c.getReal() + v * c1.getReal(), c.getImaginary() + v
				* c1.getImaginary());
	}
}
