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
import org.apache.commons.math3.complex.ComplexUtils;

/**
 * Contains the coefficients of a 2nd order digital filter with two poles and two zeros
 */
public class Biquad {

    double m_a0;
    double m_a1;
    double m_a2;
    double m_b1;
    double m_b2;
    double m_b0;

    public double getA0() {
        return m_a0;
    }

    public double getA1() {
        return m_a1 * m_a0;
    }

    public double getA2() {
        return m_a2 * m_a0;
    }

    public double getB0() {
        return m_b0 * m_a0;
    }

    public double getB1() {
        return m_b1 * m_a0;
    }

    public double getB2() {
        return m_b2 * m_a0;
    }

    public void setCoefficients(double a0, double a1, double a2,
                         double b0, double b1, double b2) {
        m_a0 = a0;
        m_a1 = a1 / a0;
        m_a2 = a2 / a0;
        m_b0 = b0 / a0;
        m_b1 = b1 / a0;
        m_b2 = b2 / a0;
    }

    public void setOnePole(Complex pole, Complex zero) {
        double a0 = 1;
        double a1 = -pole.getReal();
        double a2 = 0;
        double b0 = -zero.getReal();
        double b1 = 1;
        double b2 = 0;
        setCoefficients(a0, a1, a2, b0, b1, b2);
    }

    public void setTwoPole(Complex pole1, Complex zero1,
                    Complex pole2, Complex zero2) {
        double a0 = 1;
        double a1;
        double a2;

        if (pole1.getImaginary() != 0) {

            a1 = -2 * pole1.getReal();
            a2 = pole1.abs() * pole1.abs();
        } else {

            a1 = -(pole1.getReal() + pole2.getReal());
            a2 = pole1.getReal() * pole2.getReal();
        }

        double b0 = 1;
        double b1;
        double b2;

        if (zero1.getImaginary() != 0) {

            b1 = -2 * zero1.getReal();
            b2 = zero1.abs() * zero1.abs();
        } else {

            b1 = -(zero1.getReal() + zero2.getReal());
            b2 = zero1.getReal() * zero2.getReal();
        }

        setCoefficients(a0, a1, a2, b0, b1, b2);
    }

    public void applyScale(double scale) {
        m_b0 *= scale;
        m_b1 *= scale;
        m_b2 *= scale;
    }


    public void setPoleZeroPair(PoleZeroPair pair) {
        if (pair.isSinglePole()) {
            setOnePole(pair.poles.first, pair.zeros.first);
        } else {
            setTwoPole(pair.poles.first, pair.zeros.first,
                    pair.poles.second, pair.zeros.second);
        }
    }
}
