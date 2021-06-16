import { IElasticConfig } from "./../../interfaces/elastic-config";

export const elasticConfig: IElasticConfig = {
  url: "http://127.0.0.1:9200/",
  username: "elastic",
  password: "changeme",
  datasetsInfoIndex: "datasets_info",
  riskAnalysisInfoIndex: "risk_analysis_info",
  anonymizationInfoIndex: "anonymization_info",
  dataReloadTime: 5000
}