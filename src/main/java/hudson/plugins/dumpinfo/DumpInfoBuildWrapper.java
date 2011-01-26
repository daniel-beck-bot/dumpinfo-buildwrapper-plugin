/*
 * The MIT License
 * 
 * Copyright (c) 2011, Jesse Farinacci
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package hudson.plugins.dumpinfo;

import hudson.Extension;
import hudson.Launcher;
import hudson.PluginWrapper;
import hudson.PluginManager;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.model.JDK;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;

import java.io.IOException;
import java.io.PrintStream;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * This plugin allows jobs to automatically dump some important Hudson-specific
 * information into the job log.
 * 
 * @author <a href="mailto:jieryn@gmail.com">Jesse Farinacci</a>
 * @since 1.393
 */
public final class DumpInfoBuildWrapper extends BuildWrapper
{
  /**
   * Plugin marker for BuildWrapper.
   */
  @Extension
  public static class DescriptorImpl extends BuildWrapperDescriptor
  {
    @Override
    public String getDisplayName()
    {
      return Messages.DumpInfo_DisplayName();
    }

    @Override
    public boolean isApplicable(final AbstractProject<?, ?> item)
    {
      return true;
    }
  }

  /**
   * Whether or not to dump information about Hudson slave computers.
   * 
   * @see Hudson#getComputers()
   */
  private final boolean dumpComputers;

  /**
   * Whether or not to dump information about Hudson JDK tools.
   * 
   * @see Hudson#getJDKs()
   */
  private final boolean dumpJdks;

  /**
   * Whether or not to dump information about Hudson plugins.
   * 
   * @see Hudson#getPluginManager()
   * @see PluginManager#getPlugins()
   */
  private final boolean dumpPlugins;

  /**
   * Configuration of this plugin is per-job.
   * 
   * @param dumpComputers
   *          whether or not to dump information about Hudson slave computers.
   * @param dumpJdks
   *          whether or not to dump information about Hudson JDK tools.
   * @param dumpPlugins
   *          whether or not to dump information about Hudson plugins.
   */
  @DataBoundConstructor
  public DumpInfoBuildWrapper(final boolean dumpComputers,
      final boolean dumpJdks, final boolean dumpPlugins)
  {
    super();

    this.dumpComputers = dumpComputers;
    this.dumpJdks = dumpJdks;
    this.dumpPlugins = dumpPlugins;
  }

  /**
   * Get whether or not to dump information about Hudson slave computers.
   * 
   * @return whether or not to dump information about Hudson slave computers
   */
  public boolean isDumpComputers()
  {
    return dumpComputers;
  }

  /**
   * Get whether or not to dump information about Hudson JDK tools.
   * 
   * @return whether or not to dump information about Hudson JDK tools
   */
  public boolean isDumpJdks()
  {
    return dumpJdks;
  }

  /**
   * Get whether or not to dump information about Hudson plugins.
   * 
   * @return whether or not to dump information about Hudson plugins
   */
  public boolean isDumpPlugins()
  {
    return dumpPlugins;
  }

  @Override
  public BuildWrapper.Environment setUp(
      @SuppressWarnings("rawtypes") final AbstractBuild build,
      final Launcher launcher, final BuildListener listener)
      throws IOException, InterruptedException
  {
    final Hudson hudson = Hudson.getInstance();
    final PrintStream logger = listener.getLogger();

    // ---

    logger.println(MessagesUtils.format(Hudson.getInstance()));

    if (dumpComputers)
    {
      for (final Computer computer : hudson.getComputers())
      {
        logger.println(MessagesUtils.format(computer));
      }
    }

    if (dumpJdks)
    {
      for (final JDK jdk : hudson.getJDKs())
      {
        logger.println(MessagesUtils.format(jdk));
      }
    }

    if (dumpPlugins)
    {
      for (final PluginWrapper plugin : hudson.getPluginManager().getPlugins())
      {
        logger.println(MessagesUtils.format(plugin));
      }
    }

    // ---

    return new Environment()
    {
      /* empty implementation */
    };
  }
}
