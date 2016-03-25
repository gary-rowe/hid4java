/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Gary Rowe
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package org.hid4java;

/**
 * <p>Factory to provide the following to API consumers:</p>
 * <ul>
 * <li>Access to configured HID services</li>
 * </ul>
 *
 * @since 0.0.1
 *  
 */
public class HidManager
{

    private static final Object servicesLock = new Object();

    private static HidServices hidServices = null;


    /**
     * Create HidServices with scanInterval of 500 ms and autoShutdown enabled.
     *
     * @return A single instance of the HID services
     */
    public static HidServices getHidServices() throws HidException
    {
        return getHidServices(true, 500);
    }


    /**
     *  Create HidServices with scanInterval of 500 ms and parameter for autoShutdown.
     *
     *  @param autoShutdown true is API should autoShutdown
     *  @return A single instance of the HID services
     */
    public static HidServices getHidServices(boolean autoShutdown) throws HidException
    {
        return getHidServices(autoShutdown, 500);
    }


    /**
     * Create HidServices with autoShutdown enabled and scanInterval parameter (0 = disabled)
     *
     * @param scanInterval scan interval (0 = disabled)
     * @return A single instance of the HID services
     */
    public static HidServices getHidServices(int scanInterval) throws HidException
    {
        return getHidServices(true, scanInterval);
    }


    /**
     * Create HidServices with parameters for autoShutdown and scanInterval 
     *
     * @param autoShutdown true is API should autoShutdown
     * @param scanInterval scan interval (0 = disabled)
     * @return A single instance of the HID services
     */
    public static HidServices getHidServices(boolean autoShutdown, int scanInterval) throws HidException
    {
        synchronized (servicesLock)
        {
            if (hidServices == null)
            {
                hidServices = new HidServices(autoShutdown, scanInterval);
            }
        }

        return hidServices;
    }

}
