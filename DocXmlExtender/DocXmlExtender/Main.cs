using System;
using System.Linq;
using System.Threading;
using System.Xml;
using System.Reflection;
using System.Threading.Tasks;

namespace DocXmlExtender
{
	class MainClass
	{
	    private static readonly ManualResetEvent HadError = new ManualResetEvent(false);

		public static void Main (string[] args)
		{
			if (args.Length == 3)
			{
				string docXmlPath = args[0];
				string assemblyPath = args[1];
				string outputPath = args[2];

				var docXml = new XmlDocument();
                Assembly assembly = null;

			    AppDomain.CurrentDomain.ReflectionOnlyAssemblyResolve += ReflectionOnlyAssemblyResolver;
                
			    var loadXmlTask = Task.Factory.StartNew(
			        delegate
			            {
			                try
			                {
			                    docXml.Load(docXmlPath);
			                }
                            catch (Exception ex)
                            {
                                ReportError("Failed to load doc-xml", ex);
                            }
			            });
                var loadDllTask = Task.Factory.StartNew(
			        delegate
			            {
                            try
                            {
                                assembly = Assembly.ReflectionOnlyLoadFrom(assemblyPath);                                
                            }
                            catch (Exception ex)
                            {
                                ReportError("Failed to load assembly", ex);
                            }
			            });
			    
			    Task.WaitAll(loadXmlTask, loadDllTask);

                if (!HadError.WaitOne(0))
                {
                    var extender = new Extender(assembly);
                    var memberNodes = docXml.SelectNodes("/doc/members/member");
                    if (memberNodes != null)
                    {
                        try
                        {
                            Parallel.ForEach(memberNodes.OfType<XmlElement>(), extender.ExtendMemberNode);
                        }
                        catch (Exception ex)
                        {
                            ReportError("Error during extending members", ex);
                            return;
                        }
                    }

                    try
                    {
                        docXml.Save(outputPath);
                    }
                    catch (Exception ex)
                    {
                        ReportError("Could not save extended doc-xml file", ex);
                    }
                }
			}
			else
			{
				Console.WriteLine ("Usage: DocXmlExtender <docxml> <assembly> <output>");
			}
		}

	    private static Assembly ReflectionOnlyAssemblyResolver(object sender, ResolveEventArgs args)
	    {
	        return Assembly.ReflectionOnlyLoad(args.Name);
	    }

	    public static void ReportError(string task, Exception ex)
		{
			Console.Error.WriteLine("{0}: {1}", task, ex.Message);
			Console.Error.WriteLine(ex.ToString());
		    HadError.Set();
		}
	}
}
