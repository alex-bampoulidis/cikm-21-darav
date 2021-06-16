import { IMethodsConfig, typesOfMethods, typesOfOptions, DataTypes, IDataTypesMethods, ProcTypes } from "../../interfaces/anon-config";
import { ImenuData } from "../../interfaces/menu-data";
import { IdatasetOptions } from "./../../interfaces/elastic-config";

export const menuData: ImenuData[] = [
    // {
    //     url: "/home",
    //     iconUrl: "./../../assets/icons/home_icon_white.svg",
    //     name: "Home",
    //     id: "home"
    // },
    {
        url: "/datasets",
        iconUrl: "./../../assets/icons/data_search_icon_white.svg",
        name: "Datasets",
        id: "datasets"
    },
    {
        url: "/risk_analysis",
        iconUrl: "./../../assets/icons/risk_analysis_icon_white.svg",
        name: "Risk Analysis",
        id: "risk_analysis"
    },
    {
        url: "/anonymization",
        iconUrl: "./../../assets/icons/anon_icon_white.svg",
        name: "Anonymization",
        id: "anonymization"
    },
    {
        url: "",
        iconUrl: "",
        name: "_hr_",
        id: ""
    },
    {
        url: "/processing_queue",
        iconUrl: "./../../assets/icons/queue_icon_white.svg",
        name: "Processing queue",
        id: "processing_queue"
    }
];

export const readyDatasetOptions: IdatasetOptions[] = [
    // {
    //     optioniconUrl: "./../../assets/icons/file_icon_blue.svg",
    //     optionName: "Open",
    //     optionUrlInParts: "",
    //     optionEval: true,
    //     optionIsAction: false,
    //     optionAction: (indexId, elastic) => { }
    // },
    {
        optioniconUrl: "./../../assets/icons/pen_icon_blue.svg",
        optionName: "Edit",
        optionUrlInParts: ["/datasets/"],
        optionEval: true,
        optionIsAction: false,
        optionAction: (indexId, elastic) => { }
    },
    {
        optioniconUrl: "./../../assets/icons/risk_analysis_icon_blue.svg",
        optionName: "Risk Analysis",
        optionUrlInParts: ["/datasets/", "/risk_analysis"],
        optionEval: true,
        optionIsAction: false,
        optionAction: (indexId, elastic) => { }
    },
    {
        optioniconUrl: "./../../assets/icons/anon_icon_blue.svg",
        optionName: "Anonymization",
        optionUrlInParts: ["/datasets/", "/anonymization"],
        optionEval: true,
        optionIsAction: false,
        optionAction: (indexId, elastic) => { }
    }, {
        optioniconUrl: "",
        optionName: "_hr_",
        optionUrlInParts: [],
        optionEval: true,
        optionIsAction: false,
        optionAction: (indexId, elastic) => { }
    }, {
        optioniconUrl: "./../../assets/icons/bin_icon_blue.svg",
        optionName: "Delete",
        optionUrlInParts: [],
        optionEval: true,
        optionIsAction: true,
        optionAction: (indexId, elastic) => { return elastic.deleteDataset(indexId); }
    }
];

export const notReadyDatasetOptions: IdatasetOptions[] = [
    // {
    //     optioniconUrl: "./../../assets/icons/file_icon_blue.svg",
    //     optionName: "Open",
    //     optionUrlInParts: "",
    //     optionEval: false,
    //     optionIsAction: false,
    //     optionAction: (indexId, elastic) => { }
    // },
    {
        optioniconUrl: "./../../assets/icons/pen_icon_blue.svg",
        optionName: "Edit",
        optionUrlInParts: ["/datasets/"],
        optionEval: true,
        optionIsAction: false,
        optionAction: (indexId, elastic) => { }
    },
    {
        optioniconUrl: "./../../assets/icons/risk_analysis_icon_blue.svg",
        optionName: "Risk Analysis",
        optionUrlInParts: ["/datasets/", "/risk_analysis"],
        optionEval: false,
        optionIsAction: false,
        optionAction: (indexId, elastic) => { }
    },
    {
        optioniconUrl: "./../../assets/icons/anon_icon_blue.svg",
        optionName: "Anonymization",
        optionUrlInParts: ["/datasets/", "/anonymization"],
        optionEval: false,
        optionIsAction: false,
        optionAction: (indexId, elastic) => { }
    }, {
        optioniconUrl: "",
        optionName: "_hr_",
        optionUrlInParts: [],
        optionEval: true,
        optionIsAction: false,
        optionAction: (indexId, elastic) => { }
    }, {
        optioniconUrl: "./../../assets/icons/bin_icon_blue.svg",
        optionName: "Delete",
        optionUrlInParts: [],
        optionEval: true,
        optionIsAction: true,
        optionAction: (indexId, elastic) => { return elastic.deleteDataset(indexId); }
    }
];

export const readyRiskProcessOptions: IdatasetOptions[] = [
    {
        optioniconUrl: "./../../assets/icons/file_icon_blue.svg",
        optionName: "See Results",
        optionUrlInParts: ["/risk_analysis/"],
        optionEval: true,
        optionIsAction: false,
        optionAction: (indexId, elastic) => { }
    },
    {
        optioniconUrl: "",
        optionName: "_hr_",
        optionUrlInParts: [],
        optionEval: true,
        optionIsAction: false,
        optionAction: (indexId, elastic) => { }
    },
    {
        optioniconUrl: "./../../assets/icons/bin_icon_blue.svg",
        optionName: "Delete",
        optionUrlInParts: [],
        optionEval: true,
        optionIsAction: true,
        optionAction: (indexId, elastic) => { return elastic.deleteProcess(indexId, ProcTypes.Risk); }
    }
];

export const notReadyRiskProcessOptions: IdatasetOptions[] = [
    {
        optioniconUrl: "./../../assets/icons/file_icon_blue.svg",
        optionName: "See Results",
        optionUrlInParts: ["/risk_analysis/"],
        optionEval: true,
        optionIsAction: false,
        optionAction: (indexId, elastic) => { }
    },
    {
        optioniconUrl: "",
        optionName: "_hr_",
        optionUrlInParts: [],
        optionEval: true,
        optionIsAction: false,
        optionAction: (indexId, elastic) => { }
    },
    {
        optioniconUrl: "./../../assets/icons/bin_icon_blue.svg",
        optionName: "Delete",
        optionUrlInParts: [],
        optionEval: false,
        optionIsAction: true,
        optionAction: (indexId, elastic) => { return elastic.deleteProcess(indexId, ProcTypes.Risk); }
    }
];

export const readyAnonProcessOptions: IdatasetOptions[] = [
    {
        optioniconUrl: "./../../assets/icons/file_icon_blue.svg",
        optionName: "See Results",
        optionUrlInParts: ["/anonymization/"],
        optionEval: true,
        optionIsAction: false,
        optionAction: (indexId, elastic) => { }
    },
    {
        optioniconUrl: "",
        optionName: "_hr_",
        optionUrlInParts: [],
        optionEval: true,
        optionIsAction: false,
        optionAction: (indexId, elastic) => { }
    },
    {
        optioniconUrl: "./../../assets/icons/bin_icon_blue.svg",
        optionName: "Delete",
        optionUrlInParts: [],
        optionEval: true,
        optionIsAction: true,
        optionAction: (indexId, elastic) => { return elastic.deleteProcess(indexId, ProcTypes.Anon); }
    }
];

export const notReadyAnonProcessOptions: IdatasetOptions[] = [
    {
        optioniconUrl: "./../../assets/icons/file_icon_blue.svg",
        optionName: "See Results",
        optionUrlInParts: ["/anonymization/"],
        optionEval: false,
        optionIsAction: false,
        optionAction: (indexId, elastic) => { }
    },
    {
        optioniconUrl: "",
        optionName: "_hr_",
        optionUrlInParts: [],
        optionEval: true,
        optionIsAction: false,
        optionAction: (indexId, elastic) => { }
    },
    {
        optioniconUrl: "./../../assets/icons/bin_icon_blue.svg",
        optionName: "Delete",
        optionUrlInParts: [],
        optionEval: false,
        optionIsAction: true,
        optionAction: (indexId, elastic) => { return elastic.deleteProcess(indexId, ProcTypes.Anon); }
    }
];

export const DataTypesMethods: IDataTypesMethods[] = [
    {
        id: DataTypes.Aggregated,
        methods: [typesOfMethods.Aggregated]
    },
    {
        id: DataTypes.Tabular,
        methods: [typesOfMethods.Kanonymity, typesOfMethods.Ldiversity]
    },
    {
        id: DataTypes.Invoices,
        methods: [typesOfMethods.Invoices]
    },
    {
        id: DataTypes.Location,
        methods: [typesOfMethods.Location]
    },
    {
        id: DataTypes.Textual,
        methods: [typesOfMethods.Textual]
    }
];

export const anonMethods: IMethodsConfig[] = [
    {
        type: typesOfMethods.Kanonymity,
        columnOptions: [
            {
                id: "QI",
                name: "Quasi Identifier",
                multiple: true,
                compulsory: true,
                description: "Quasi Identifier is"
            }
        ],
        proccessingParamOptions: [
            {
                id: "k",
                name: "k parameter",
                optionType: typesOfOptions.text,
                options: [],
                description: "k parameter is",
                textInputSize: 50
            },
            {
                id: "prevKAnon",
                name: "Already k-anonymized",
                optionType: typesOfOptions.select,
                options: ["Yes", "No"],
                description: "k-Anonymity is",
                textInputSize: null
            }
        ],
        riskAnalysisParamOptions: []
    },
    {
        type: typesOfMethods.Ldiversity,
        columnOptions: [
            {
                id: "QI",
                name: "Quasi Identifier",
                multiple: true,
                compulsory: true,
                description: "A Quasi Identifier is"
            },
            {
                id: "sensitive",
                name: "Sensitive Attribute",
                multiple: true,
                compulsory: true,
                description: "A Sensitive Attribute is"
            }
        ],
        proccessingParamOptions: [
            {
                id: "l",
                name: "l parameter",
                optionType: typesOfOptions.text,
                options: [],
                description: "l parameter is",
                textInputSize: 50
            },
            {
                id: "prevKAnon",
                name: "Already k-anonymized",
                optionType: typesOfOptions.select,
                options: ["Yes", "No"],
                description: "k-Anonymity is",
                textInputSize: null
            }
        ],
        riskAnalysisParamOptions: []
    },
    {
        type: typesOfMethods.Aggregated,
        columnOptions: [
            {
                id: "QI",
                name: "Quasi Identifier",
                multiple: true,
                compulsory: true,
                description: "Quasi Identifier is"
            }
        ],
        proccessingParamOptions: [
            {
                id: "k",
                name: "k parameter",
                optionType: typesOfOptions.text,
                options: [],
                description: "k parameter is",
                textInputSize: 50
            },
            {//not sure if needed
                id: "prevKAnon",
                name: "Already k-anonymized",
                optionType: typesOfOptions.select,
                options: ["Yes", "No"],
                description: "k-Anonymity is",
                textInputSize: null
            }
        ],
        riskAnalysisParamOptions: []
    },
    {
        type: typesOfMethods.Invoices,
        columnOptions: [
            {
                id: "individualId",
                name: "Individual Identifier",
                multiple: false,
                compulsory: true,
                description: "Individual Identifier is"
            },
            {
                id: "invoiceDate",
                name: "Invoice Date",
                multiple: false,
                compulsory: true,
                description: "Invoice Date is"
            },
            {
                id: "invoiceAmount",
                name: "Invoice Amount",
                multiple: false,
                compulsory: true,
                description: "Invoice Amount is"
            }
        ],
        proccessingParamOptions: [
            // {
            //     id: "dateFormat",
            //     name: "Date format",
            //     optionType: typesOfOptions.select,
            //     options: ["yy.MM"],
            //     description: "Number of other Individuals is"
            // }
            {
                id: "dateFormat",
                name: "Date format",
                optionType: typesOfOptions.text,
                options: [],
                description: "Number of other Individuals is",
                textInputSize: 200
            }
        ],
        riskAnalysisParamOptions: [
            {
                id: "noOfOtherIndividuals",
                name: "Number of other Individuals",
                optionType: typesOfOptions.text,
                options: [],
                description: "Number of other Individuals is",
                textInputSize: 50
            }
            , {
                id: "invoiceAmount",
                name: "Invoice Amount Within",
                optionType: typesOfOptions.text,
                options: [],
                description: "Invoice Amount Within is",
                textInputSize: 100
            }
            , {
                id: "timeframe",
                name: "Within a timeframe of",
                optionType: typesOfOptions.textAndSelect,
                options: ["minutes", "hours", "days", "weeks", "months"],
                description: "Within a timeframe of is",
                textInputSize: 50
            }
        ]
    },
    {
        type: typesOfMethods.Location,
        columnOptions: [
            {
                id: "userid",
                name: "userID",
                multiple: false,
                compulsory: true,
                description: "Individual Identifier is"
            },
            {
                id: "latitude",
                name: "Latitude",
                multiple: false,
                compulsory: true,
                description: "Individual Identifier is"
            },
            {
                id: "longtitude",
                name: "Longtitude",
                multiple: false,
                compulsory: true,
                description: "Individual Identifier is"
            },
            {
                id: "time",
                name: "Timestamp",
                multiple: false,
                compulsory: true,
                description: "Individual Identifier is"
            },
            {
                id: "locationid",
                name: "locationID",
                multiple: false,
                compulsory: false,
                description: "Individual Identifier is"
            }
        ],
        proccessingParamOptions: [
            {
                id: "timeformat",
                name: "Time format",
                optionType: typesOfOptions.text,
                options: [],
                description: "Number of other Individuals is",
                textInputSize: 200
            }
        ],
        riskAnalysisParamOptions: [
            {
                id: "users",
                name: "Number of users",
                optionType: typesOfOptions.text,
                options: [],
                description: "Number of other Individuals is",
                textInputSize: 50
            }
            ,
            {
                id: "radius",
                name: "Radius in km",
                optionType: typesOfOptions.text,
                options: [],
                description: "Invoice Amount Within is",
                textInputSize: 50
            }
            ,
            {
                id: "timewithin",
                name: "Within time",
                optionType: typesOfOptions.text,
                options: [],
                description: "Invoice Amount Within is",
                textInputSize: 50
            },
            {
                id: "timeframe",
                name: "Within a timeframe of",
                optionType: typesOfOptions.select,
                options: ["minutes", "hours", "days", "weeks", "months"],
                description: "Within a timeframe of is",
                textInputSize: null
            }
        ]
    },
    {
        type: typesOfMethods.Textual,
        columnOptions: [
            {
                id: "userid",
                name: "userID",
                multiple: false,
                compulsory: true,
                description: "Individual Identifier is"
            },
            {
                id: "textcolumn",
                name: "Text",
                multiple: false,
                compulsory: true,
                description: "Individual Identifier is"
            }
        ],
        proccessingParamOptions: [

        ],
        riskAnalysisParamOptions: [

        ]
    }
];