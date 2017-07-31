import { Component } from '@angular/core';
import { IonicPage, NavController, NavParams } from 'ionic-angular';
import { QueriesPage } from '../queries/queries';
/**
 * Generated class for the NewqueryPage page.
 *
 * See http://ionicframework.com/docs/components/#navigation for more info
 * on Ionic pages and navigation.
 */
@IonicPage()
@Component({
    selector: 'page-newquery',
    templateUrl: 'newquery.html',
})
export class NewqueryPage {
    query: {};

    constructor(public navCtrl: NavController, public navParams: NavParams) {
        this.query = {};
    }

    addQuery() {
        var item = {};
        item["question"] = this.query["question"];
        item["totalVotes"] = 0;
        item["showDetails"] = false;
        item["answers"] = [];
        item["answers"].push({
            A: this.query["answer1"],
            Count: 0
        });

        item["answers"].push({
            A: this.query["answer2"],
            Count: 0
        });

        if (this.query["answer3"] != undefined && this.query["answer3"] != "") {
            item["answers"].push({
                A: this.query["answer3"],
                Count: 0
            });
        }

        item["tags"] = this.query["tags"];
        this.navCtrl.push(QueriesPage, {
            item: item
        });
    }

    ionViewDidLoad() {
        console.log('ionViewDidLoad NewqueryPage');
    }
}
